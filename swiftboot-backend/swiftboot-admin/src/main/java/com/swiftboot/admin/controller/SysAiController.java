package com.swiftboot.admin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.swiftboot.common.security.utils.SecurityUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * AI 智能助手控制器
 * 负责处理前端 AI 助手的对话请求，集成 RAG（检索增强生成）支持。
 * 当前仅支持 DeepSeek 模型。
 *
 * 主要功能：
 * 1. 接收用户问题，判断是否需要检索向量库（RAG）。
 * 2. 调用 Python 向量检索服务获取相关上下文。
 * 3. 组装 System Prompt（包含 Skills 技能库 + RAG 上下文）。
 * 4. 调用 DeepSeek AI 模型（支持流式/非流式）。
 * 5. 管理对话历史（Redis 短期记忆 + ChromaDB 长期记忆）。
 */
@Tag(name = "AI助手")
@RestController
@RequestMapping("/system/ai")
public class SysAiController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // Redis 历史记录 Key 前缀
    private static final String HISTORY_KEY_PREFIX = "ai:history:";
    // 向量检索的最大距离阈值（越小越相似，0.6 为经验值）
    private static final double MEMORY_MAX_DISTANCE = 0.6;

    // DeepSeek 配置
    @Value("${ai.deepseek.api-url:}")
    private String deepseekApiUrl;

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model:}")
    private String deepseekModel;

    // 缓存项目内置的 Skills (技能库) 内容，避免每次请求都读取文件
    private String skillsContext = "";
    
    // Python 检索引擎地址 (RAG 服务)
    // /retrieve: 检索代码库知识
    // /memory/query: 检索历史对话记忆
    // /memory/add: 存储新的对话记忆
    private static final String RAG_API_URL = "http://localhost:8001/retrieve";
    private static final String MEMORY_QUERY_URL = "http://localhost:8001/memory/query";
    private static final String MEMORY_ADD_URL = "http://localhost:8001/memory/add";

    /**
     * 初始化 Skills 技能库
     * 系统启动时，扫描项目根目录下的 `project-skills` 文件夹，读取所有 markdown 文件。
     * 这些内容会被作为 System Prompt 的一部分，赋予 AI 项目特定的知识。
     */
    @PostConstruct
    public void initSkills() {
        try {
            // 扫描项目根目录下的 project-skills 目录
            // 注意：实际部署时路径可能需要调整，这里适配了本地开发环境的多模块结构
            String projectRoot = System.getProperty("user.dir");
            // 如果是模块运行，可能需要回退一级目录找到根目录
            if (projectRoot.endsWith("swiftboot-admin")) {
                projectRoot = new File(projectRoot).getParentFile().getParent();
            } else if (projectRoot.endsWith("swiftboot-backend")) {
                projectRoot = new File(projectRoot).getParent();
            }
            
            File skillsDir = new File(projectRoot, "project-skills");
            if (skillsDir.exists() && skillsDir.isDirectory()) {
                StringBuilder sb = new StringBuilder();
                sb.append("以下是项目已有的 Skills 知识库，当用户问题涉及相关领域时，请优先参考这些内容：\n\n");
                
                File[] skillFolders = skillsDir.listFiles(File::isDirectory);
                if (skillFolders != null) {
                    for (File folder : skillFolders) {
                        File skillFile = new File(folder, "SKILL.md");
                        if (skillFile.exists()) {
                            String content = FileUtil.readString(skillFile, StandardCharsets.UTF_8);
                            // 简单的 frontmatter 解析 (移除文件头部的 YAML 配置信息)
                            String name = folder.getName();
                            if (content.startsWith("---")) {
                                int endIndex = content.indexOf("---", 3);
                                if (endIndex > 0) {
                                    content = content.substring(endIndex + 3).trim();
                                }
                            }
                            sb.append("=== Skill: ").append(name).append(" ===\n");
                            sb.append(content).append("\n\n");
                        }
                    }
                }
                skillsContext = sb.toString();
                System.out.println("Skills loaded successfully, length: " + skillsContext.length());
            } else {
                System.out.println("Skills directory not found: " + skillsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load skills: " + e.getMessage());
        }
    }

    /**
     * 发送普通对话 (非流式)
     * 适用于简单的问答场景，前端等待完整响应后一次性展示。
     */
    @Operation(summary = "发送对话")
    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return R.fail("内容不能为空");
        }

        // 1. 构建 System Prompt
        String tempSystemPrompt = "你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。请用简洁、专业的语言回答用户关于开发、代码或项目管理的问题。";
        if (StrUtil.isNotEmpty(skillsContext)) {
            tempSystemPrompt += "\n\n" + skillsContext;
        }
        final String systemPrompt = tempSystemPrompt;
        Long userId = SecurityUtils.getUserId();
        
        try {
            HttpResponse response;
            
            // --- DeepSeek 调用逻辑 ---
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", deepseekModel);
            requestBody.set("stream", false);

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.set("role", "system");
            systemMessage.set("content", systemPrompt);
            messages.add(systemMessage);

            // 2. 记忆检索 (RAG - 历史对话)
            // 如果问题包含特定关键词，尝试从向量库检索相关的历史对话
            if (shouldQueryVector(content)) {
                try {
                    JSONObject memoryReq = new JSONObject();
                    memoryReq.set("user_id", String.valueOf(userId));
                    memoryReq.set("question", content);
                    memoryReq.set("n_results", 6);
                    memoryReq.set("max_distance", MEMORY_MAX_DISTANCE);
                    String memoryResp = HttpRequest.post(MEMORY_QUERY_URL)
                            .timeout(5000)
                            .body(memoryReq.toString())
                            .execute()
                            .body();
                    JSONObject memoryJson = JSONUtil.parseObj(memoryResp);
                    JSONArray results = memoryJson.getJSONArray("results");
                    if (results != null) {
                        // 将检索到的历史记忆插入到 messages 中
                        for (int i = 0; i < results.size(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            JSONObject meta = item.getJSONObject("metadata");
                            String memContent = item.getStr("content");
                            String role = meta != null ? meta.getStr("role") : null;
                            if (StrUtil.isNotBlank(memContent) && StrUtil.isNotBlank(role) && !"system".equals(role)) {
                                JSONObject msg = new JSONObject();
                                msg.set("role", role);
                                msg.set("content", memContent);
                                messages.add(msg);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Memory query failed: " + e.getMessage());
                }
            }

            // 3. 注入近期对话历史 (Redis Short-Term Memory)
            // 无论是否触发 RAG，都携带最近的 N 条对话记录，保证上下文连贯性
            addRecentHistory(messages, userId);

            JSONObject userMessage = new JSONObject();
            userMessage.set("role", "user");
            userMessage.set("content", content);
            messages.add(userMessage);

            requestBody.set("messages", messages);

            System.out.println("Sending AI request to: " + deepseekApiUrl);
            response = HttpRequest.post(deepseekApiUrl)
                    .timeout(90000)
                    .header("Authorization", "Bearer " + deepseekApiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            // 3. 处理响应
            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                if (jsonResponse.containsKey("error")) {
                     return R.fail("AI 服务错误: " + jsonResponse.getJSONObject("error").getStr("message"));
                }
                String reply = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");

                // 4. 存储新记忆 (RAG - 写入)
                try {
                    long now = System.currentTimeMillis();
                    JSONObject memoryAdd = new JSONObject();
                    memoryAdd.set("user_id", String.valueOf(userId));
                    JSONArray memoryMessages = new JSONArray();
                    // 同时存入用户问题和 AI 回复
                    memoryMessages.add(new JSONObject().set("content", content).set("role", "user").set("timestamp", now).set("sequence", now));
                    memoryMessages.add(new JSONObject().set("content", reply).set("role", "assistant").set("timestamp", now + 1).set("sequence", now + 1));
                    memoryAdd.set("messages", memoryMessages);
                    HttpRequest.post(MEMORY_ADD_URL)
                            .timeout(5000)
                            .body(memoryAdd.toString())
                            .execute();
                } catch (Exception e) {
                    System.err.println("Memory add failed: " + e.getMessage());
                }
                return R.ok("操作成功", reply);
            } else {
                if (response.getStatus() == 401) {
                    return R.fail("API Key 无效或过期，请在 application-dev.yml 中配置正确的 API Key");
                }
                if (response.getStatus() == 402) {
                    return R.fail("API Key 余额不足，请充值");
                }
                return R.fail("AI 服务响应异常: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("AI 服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取 Redis 中的短期历史记录
     * 用于前端聊天界面初始化时展示最近的几条对话。
     */
    @Operation(summary = "获取历史记录")
    @GetMapping("/history")
    public R<List<JSONObject>> getHistory() {
        Long userId = SecurityUtils.getUserId();
        String key = HISTORY_KEY_PREFIX + userId;
        // 获取 Redis List 中的所有记录
        List<String> historyStr = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (historyStr == null) {
            return R.ok();
        }
        List<JSONObject> history = historyStr.stream()
                .map(JSONUtil::parseObj)
                .collect(Collectors.toList());
        return R.ok(history);
    }

    /**
     * 发送对话 (流式 Stream)
     * 使用 SSE (Server-Sent Events) 技术，实现打字机效果。
     * 支持 RAG 代码检索 + 历史记忆检索 + 记忆存储。
     */
    @Operation(summary = "发送对话(流式)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> params) {
        final SseEmitter emitter = new SseEmitter(0L); // 0L 表示永不超时
        String content = (String) params.get("content");
        Long userId = SecurityUtils.getUserId();
        
        if (content == null || content.trim().isEmpty()) {
            try {
                JSONObject msg = new JSONObject().set("content", "内容不能为空");
                emitter.send(msg.toString());
            } catch (Exception ignored) {
            }
            emitter.complete();
            return emitter;
        }

        // 异步线程处理 AI 请求，避免阻塞主线程
        new Thread(() -> {
            StringBuilder fullReply = new StringBuilder(); // 用于收集 AI 的完整回复，以便存入历史
                try {
                    // 1. RAG 代码检索 (Retrieve)
                    // 调用 Python 服务的 /retrieve 接口，查找项目代码库中相关的片段
                    String ragContext = "";
                    if (shouldQueryVector(content)) {
                        try {
                            JSONObject ragRequest = new JSONObject();
                            ragRequest.set("question", content);
                            ragRequest.set("n_results", 3); // 获取最相关的 3 个代码片段
                            
                            System.out.println("Calling RAG Engine: " + RAG_API_URL);
                            String ragResponse = HttpRequest.post(RAG_API_URL)
                                    .timeout(5000)
                                    .body(ragRequest.toString())
                                    .execute()
                                    .body();
                            
                            JSONObject ragJson = JSONUtil.parseObj(ragResponse);
                            JSONArray results = ragJson.getJSONArray("results");
                            if (results != null && !results.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("\n\n=== 参考项目代码上下文 ===\n");
                                for (int i = 0; i < results.size(); i++) {
                                    JSONObject item = results.getJSONObject(i);
                                    JSONObject meta = item.getJSONObject("metadata");
                                    String codeContent = item.getStr("content");
                                    String source = meta != null ? meta.getStr("source") : null;
                                    if (StrUtil.isNotBlank(codeContent)) {
                                        sb.append("--- Source: ").append(StrUtil.isNotBlank(source) ? source : "unknown").append(" ---\n");
                                        sb.append(codeContent).append("\n");
                                    }
                                }
                                sb.append("=========================\n");
                                ragContext = sb.toString();
                                System.out.println("RAG Context found, length: " + ragContext.length());
                            }
                        } catch (Exception e) {
                            System.err.println("RAG Engine call failed: " + e.getMessage());
                        }
                    }

                // 2. 组装 System Prompt (Augment)
                String tempSystemPrompt = "你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。请用简洁、专业的语言回答用户关于开发、代码或项目管理的问题。";
                
                // 添加 Skills (项目文档知识)
                if (StrUtil.isNotEmpty(skillsContext)) {
                    tempSystemPrompt += "\n\n" + skillsContext;
                }
                
                // 添加 RAG 上下文 (代码库知识)
                if (StrUtil.isNotEmpty(ragContext)) {
                    tempSystemPrompt += ragContext;
                    tempSystemPrompt += "\n\n请优先根据上述【参考项目代码上下文】来回答用户的问题。如果上下文中没有相关信息，再根据你的通用知识回答。";
                }

                final String systemPrompt = tempSystemPrompt;

                // 3. 构建消息列表
                JSONArray messages = new JSONArray();
                JSONObject systemMessage = new JSONObject();
                systemMessage.set("role", "system");
                systemMessage.set("content", systemPrompt);
                messages.add(systemMessage);

                // 4. 记忆检索 (查找历史对话)
                if (shouldQueryVector(content)) {
                    try {
                        JSONObject memoryReq = new JSONObject();
                        memoryReq.set("user_id", String.valueOf(userId));
                        memoryReq.set("question", content);
                        memoryReq.set("n_results", 6);
                        memoryReq.set("max_distance", MEMORY_MAX_DISTANCE);
                        String memoryResp = HttpRequest.post(MEMORY_QUERY_URL)
                                .timeout(5000)
                                .body(memoryReq.toString())
                                .execute()
                                .body();
                        JSONObject memoryJson = JSONUtil.parseObj(memoryResp);
                        JSONArray results = memoryJson.getJSONArray("results");
                        if (results != null) {
                            for (int i = 0; i < results.size(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                JSONObject itemMeta = item.getJSONObject("metadata");
                                String memContent = item.getStr("content");
                                String role = itemMeta != null ? itemMeta.getStr("role") : null;
                                if (StrUtil.isNotBlank(memContent) && StrUtil.isNotBlank(role) && !"system".equals(role)) {
                                    JSONObject msg = new JSONObject();
                                    msg.set("role", role);
                                    msg.set("content", memContent);
                                    messages.add(msg);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Memory query failed: " + e.getMessage());
                    }
                }

                // 5. 注入近期对话历史 (Redis Short-Term Memory)
                addRecentHistory(messages, userId);

                JSONObject userMessage = new JSONObject();
                userMessage.set("role", "user");
                userMessage.set("content", content);
                messages.add(userMessage);

                // 5. 调用 AI 模型 (Generate) - DeepSeek
                JSONObject requestBody = new JSONObject();
                requestBody.set("model", deepseekModel);
                requestBody.set("stream", true);
                requestBody.set("messages", messages);

                try (HttpResponse response = HttpRequest.post(deepseekApiUrl)
                        .timeout(90000)
                        .header("Authorization", "Bearer " + deepseekApiKey)
                        .header("Content-Type", "application/json")
                        .body(requestBody.toString())
                        .execute(true)) {
                    if (!response.isOk()) {
                        JSONObject msg = new JSONObject().set("content", "AI 服务响应异常: " + response.getStatus());
                        emitter.send(msg.toString());
                        emitter.complete();
                        return;
                    }
                    // 读取流式响应
                    try (InputStream inputStream = response.bodyStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty() || !line.startsWith("data:")) {
                                continue;
                            }
                            String data = line.substring(5).trim();
                            if (data.isEmpty()) {
                                continue;
                            }
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            JSONObject json = JSONUtil.parseObj(data);
                            if (json.containsKey("error")) {
                                emitter.send(json.getStr("error"));
                                break;
                            }
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                if (delta.containsKey("content")) {
                                    String chunk = delta.getStr("content");
                                    fullReply.append(chunk); // 收集完整回复
                                    emitter.send(chunk);     // 实时推送到前端
                                }
                            }
                        }
                    }
                }

                // 6. 存储对话历史 (Memory Store)
                // 只有当 fullReply 不为空时才保存历史，防止报错中断导致空记录
                if (fullReply.length() > 0) {
                    // 6.1 存入 Redis 短期历史 (用于 /history 接口展示，保留最近 20 条)
                    JSONObject userRecord = new JSONObject().set("role", "user").set("content", content);
                    JSONObject aiRecord = new JSONObject().set("role", "assistant").set("content", fullReply.toString());
                    String key = HISTORY_KEY_PREFIX + userId;
                    try {
                        stringRedisTemplate.opsForList().rightPush(key, userRecord.toString());
                        stringRedisTemplate.opsForList().rightPush(key, aiRecord.toString());
                        if (stringRedisTemplate.opsForList().size(key) > 20) {
                            stringRedisTemplate.opsForList().trim(key, -20, -1);
                        }
                        stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
                    } catch (Exception e) {
                        System.err.println("Failed to save history to Redis: " + e.getMessage());
                    }

                    // 6.2 存入 向量数据库 长期记忆 (用于后续 RAG 检索)
                    try {
                        long now = System.currentTimeMillis();
                        JSONObject memoryAdd = new JSONObject();
                        memoryAdd.set("user_id", String.valueOf(userId));
                        JSONArray memoryMessages = new JSONArray();
                        memoryMessages.add(new JSONObject().set("content", content).set("role", "user").set("timestamp", now).set("sequence", now));
                        memoryMessages.add(new JSONObject().set("content", fullReply.toString()).set("role", "assistant").set("timestamp", now + 1).set("sequence", now + 1));
                        memoryAdd.set("messages", memoryMessages);
                        HttpRequest.post(MEMORY_ADD_URL)
                                .timeout(5000)
                                .body(memoryAdd.toString())
                                .execute();
                    } catch (Exception e) {
                        System.err.println("Memory add failed: " + e.getMessage());
                    }
                }
                
                // 发送结束标记
                emitter.send("[DONE]");
                emitter.complete();

            } catch (Exception e) {
                try {
                    JSONObject msg = new JSONObject().set("content", "AI 服务调用失败: " + e.getMessage());
                    emitter.send(msg.toString());
                } catch (Exception ignored) {
                }
                emitter.complete();
            }
        }).start();

        return emitter;
    }

    /**
     * 判断是否需要检索向量库
     * 根据用户问题中的关键词，决定是否触发 RAG 流程。
     * 避免所有问题都去检索，节省资源并减少无关上下文干扰。
     */
    private boolean shouldQueryVector(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String text = content.toLowerCase();
        // 触发关键词列表：涉及代码、数据库、历史回忆等意图
        String[] keywords = new String[]{
                "接口", "api", "controller", "service", "mapper", "sql", "数据库", "表", "字段", "报错", "异常", "堆栈",
                "代码", "类", "方法", "函数", "bug", "日志", "运行", "编译", "构建", "依赖", "配置", "yml",
                "刚才", "上次", "之前", "继续", "前面", "我们聊过", "你说过", "历史", "上下文"
        };
        return StrUtil.containsAny(text, keywords);
    }

    /**
     * 将 Redis 中的近期对话历史添加到消息列表中
     * @param messages 消息列表
     * @param userId 用户ID
     */
    private void addRecentHistory(JSONArray messages, Long userId) {
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            // 获取最近 10 条历史记录 (Redis List 尾部是最新，头部是最旧)
            // 我们取最后 10 条，保持时间顺序
            List<String> historyStr = stringRedisTemplate.opsForList().range(key, -10, -1);
            if (historyStr != null) {
                for (String str : historyStr) {
                    JSONObject historyItem = JSONUtil.parseObj(str);
                    JSONObject msg = new JSONObject();
                    msg.set("role", historyItem.getStr("role"));
                    msg.set("content", historyItem.getStr("content"));
                    messages.add(msg);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load recent history: " + e.getMessage());
        }
    }
}
