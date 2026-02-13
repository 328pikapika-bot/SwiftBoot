package com.swiftboot.admin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.event.OperLogEvent;
import com.swiftboot.admin.service.SysAiSessionService;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import com.swiftboot.common.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 智能助手控制器 - Agent -模式
 * 负责处理前端 AI 助手的对话请求，集成 Agent Function Calling 支持。
 * 
 * 【Agent 工具调用流程】：
 * 1. 用户提问 → LLM（携带 tools 定义）
 * 2. LLM 决定是否调用工具 → 如需调用，返回 tool_calls
 * 3. 执行工具（调用 RAG 检索引擎）→ 获取代码上下文
 * 4. 将工具结果发送给 LLM → LLM 生成最终回答
 * 5. 流式输出给用户
 */
@Tag(name = "智能会话")
@RestController
@RequestMapping("/system/ai")
@SuppressWarnings("unchecked")
public class SysAiController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysAiSessionService aiSessionService;
    
    @Resource
    private SysOperLogService operLogService;

    // Redis 历史记录 Key 前缀
    private static final String HISTORY_KEY_PREFIX = "ai:history:";

    // DeepSeek 配置
    @Value("${ai.deepseek.api-url:}")
    private String deepseekApiUrl;

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model:}")
    private String deepseekModel;

    // 缓存项目内置的 Skills (技能库) 内容
    private String skillsContext = "";
    
    // 缓存 RAG 提示词规则（旧模式，保留作为降级方案）
    private String ragRuleContext = "";
    
    // 缓存 Agent 模式提示词规则
    private String agentRuleContext = "";
    
    // 缓存工具搜索规则
    private String toolSearchRuleContext = "";
    
    // 实时日志流连接池
    private final List<SseEmitter> logEmitters = new CopyOnWriteArrayList<>();

    // Agent 工具定义 (Function Calling)
    // 关键在于 description 的引导性，让 LLM 主动调用工具
    private JSONArray agentTools;
    
    /**
     * 构建 Agent 工具定义
     * DeepSeek API 支持 OpenAI 兼容的 Function Calling 格式
     */
    private JSONArray buildAgentTools() {
        JSONArray tools = new JSONArray();
        
        // Tool 1: search_codebase - 代码库检索
        JSONObject searchCodebaseTool = new JSONObject();
        searchCodebaseTool.set("type", "function");
        
        JSONObject function = new JSONObject();
        function.set("name", "search_codebase");
        
        String desc = "搜索项目代码库，获取相关代码片段和业务逻辑。";
        if (StrUtil.isNotEmpty(toolSearchRuleContext)) {
            desc += "\n" + toolSearchRuleContext;
        } else {
            // Fallback description if file not found
            desc += "【必须调用此工具的场景】：\n" +
                    "1. 用户询问【业务逻辑】【功能流程】【实现原理】时；\n" +
                    "2. 用户询问【接口定义】【API】【Controller/Service/Mapper】时；\n" +
                    "3. 用户询问【数据库表结构】【字段含义】【实体类】时；\n" +
                    "4. 用户询问【报错排查】【异常处理】【Bug 分析】时；\n" +
                    "5. 用户提出任何宏观问题（如'xx是怎么实现的'、'xx的查询逻辑'）需要代码佐证时。\n" +
                    "【不需要调用的场景】：纯闲聊、通用编程知识、与本项目无关的问题。";
        }
        
        function.set("description", desc);
        
        // 定义参数
        JSONObject parameters = new JSONObject();
        parameters.set("type", "object");
        
        JSONObject properties = new JSONObject();
        JSONObject queryParam = new JSONObject();
        queryParam.set("type", "string");
        queryParam.set("description", "搜索关键词或问题描述，建议包含具体的类名、方法名、功能模块名等");
        properties.set("query", queryParam);
        
        parameters.set("properties", properties);
        parameters.set("required", new JSONArray().put("query"));
        
        function.set("parameters", parameters);
        searchCodebaseTool.set("function", function);
        tools.add(searchCodebaseTool);
        
        return tools;
    }
    
    // Python 检索引擎地址 (RAG 服务)
    private static final String RAG_API_URL = "http://localhost:8001/retrieve";
    private static final String MEMORY_QUERY_URL = "http://localhost:8001/memory/query";
    private static final String MEMORY_ADD_URL = "http://localhost:8001/memory/add";
    private static final String MEMORY_DELETE_URL = "http://localhost:8001/memory/delete";
    private static final String NLP_TOPIC_URL = "http://localhost:8001/nlp/topic";
    private static final String STATS_URL = "http://localhost:8001/stats";

    /**
     * 实时索引日志流 (SSE)
     * 用于前端监控页面 (Neural Stream)
     */
    @GetMapping(value = "/index/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter indexStream(@RequestParam(defaultValue = "100") int limit) {
        SseEmitter emitter = new SseEmitter(0L); // 永不超时
        logEmitters.add(emitter);
        
        // 发送历史日志
        try {
            // 查询最新的 limit 条 AI 引擎日志
            Page<SysOperLog> page = new Page<>(1, limit);
            LambdaQueryWrapper<SysOperLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysOperLog::getOperName, "AI Engine")
                        .orderByDesc(SysOperLog::getOperTime);
            
            Page<SysOperLog> result = operLogService.page(page, queryWrapper);
            List<SysOperLog> logs = result.getRecords();
            
            // 反转顺序，按时间正序发送（旧 -> 新）
            Collections.reverse(logs);
            
            for (SysOperLog log : logs) {
                JSONObject msg = new JSONObject();
                if (log.getOperTime() != null) {
                    msg.set("time", cn.hutool.core.date.DateUtil.format(log.getOperTime(), "HH:mm:ss"));
                }
                msg.set("msg", log.getTitle());
                // 根据日志内容简单的着色逻辑
                if (log.getTitle() != null) {
                    if (log.getTitle().contains("检测到")) {
                        msg.set("color", "text-blue-400");
                    } else if (log.getTitle().contains("完成") || log.getTitle().contains("成功")) {
                        msg.set("color", "text-emerald-400");
                    } else if (log.getTitle().contains("失败") || log.getTitle().contains("错误")) {
                        msg.set("color", "text-red-400");
                    } else {
                        msg.set("color", "text-slate-400");
                    }
                } else {
                     msg.set("color", "text-slate-400");
                }
                
                emitter.send(SseEmitter.event().data(msg.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        emitter.onCompletion(() -> logEmitters.remove(emitter));
        emitter.onTimeout(() -> logEmitters.remove(emitter));
        emitter.onError((e) -> logEmitters.remove(emitter));
        
        return emitter;
    }

    /**
     * 监听操作日志事件，并推送到 SSE 流
     */
    @EventListener
    public void handleOperLogEvent(OperLogEvent event) {
        if (logEmitters.isEmpty()) {
            return;
        }
        
        SysOperLog log = event.getOperLog();
        // 仅推送 AI 引擎产生的日志
        if ("AI Engine".equals(log.getOperName()) || 
            (log.getTitle() != null && log.getTitle().contains("检测到"))) {
            
            JSONObject msg = new JSONObject();
            if (log.getOperTime() != null) {
                msg.set("time", cn.hutool.core.date.DateUtil.format(log.getOperTime(), "HH:mm:ss"));
            }
            msg.set("msg", log.getTitle());
            // 根据日志内容简单的着色逻辑
            if (log.getTitle().contains("检测到")) {
                msg.set("color", "text-blue-400");
            } else if (log.getTitle().contains("完成") || log.getTitle().contains("成功")) {
                msg.set("color", "text-emerald-400");
            } else if (log.getTitle().contains("失败") || log.getTitle().contains("错误")) {
                msg.set("color", "text-red-400");
            } else {
                msg.set("color", "text-slate-400");
            }
            
            String jsonStr = msg.toString();
            List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
            
            for (SseEmitter emitter : logEmitters) {
                try {
                    emitter.send(SseEmitter.event().data(jsonStr));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                }
            }
            
            logEmitters.removeAll(deadEmitters);
        }
    }

    /**
     * 获取 AI 引擎统计信息
     */
    @Operation(summary = "获取AI引擎统计信息")
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        try {
            String result = HttpRequest.get(STATS_URL)
                    .timeout(2000)
                    .execute()
                    .body();
            if (StrUtil.isEmpty(result)) {
                return R.ok(Map.of("knowledge_count", 0, "memory_count", 0));
            }
            Map<String, Object> map = JSONUtil.toBean(result, Map.class);
            return R.ok(map);
        } catch (Exception e) {
            // 服务未启动或调用失败，返回默认值
            return R.ok(Map.of("knowledge_count", 0, "memory_count", 0));
        }
    }

    /**
     * 初始化 Skills 技能库和 Agent 规则
     */
    @PostConstruct
    public void initSkills() {
        try {
            // 加载 RAG 规则文件（旧模式，保留作为降级方案）
            try {
                ClassPathResource resource = new ClassPathResource("ai/rules/rag_rule.md");
                if (resource.exists()) {
                    ragRuleContext = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                    System.out.println("RAG Rule loaded successfully, length: " + ragRuleContext.length());
                }
            } catch (Exception e) {
                System.err.println("Failed to load RAG rule: " + e.getMessage());
            }
            
            // 加载 Agent 模式规则文件
            try {
                ClassPathResource agentResource = new ClassPathResource("ai/rules/agent_rule.md");
                if (agentResource.exists()) {
                    agentRuleContext = StreamUtils.copyToString(agentResource.getInputStream(), StandardCharsets.UTF_8);
                    System.out.println("Agent Rule loaded successfully, length: " + agentRuleContext.length());
                }
            } catch (Exception e) {
                System.err.println("Failed to load Agent rule: " + e.getMessage());
            }
            
            // 加载工具搜索规则文件
            try {
                ClassPathResource searchRuleResource = new ClassPathResource("ai/rules/tool_search_codebase_rule.md");
                if (searchRuleResource.exists()) {
                    toolSearchRuleContext = StreamUtils.copyToString(searchRuleResource.getInputStream(), StandardCharsets.UTF_8);
                    System.out.println("Tool Search Rule loaded successfully, length: " + toolSearchRuleContext.length());
                }
            } catch (Exception e) {
                System.err.println("Failed to load Tool Search rule: " + e.getMessage());
            }
            
            // 初始化工具定义 (需在规则加载后执行)
            this.agentTools = buildAgentTools();

            // 扫描项目根目录下的 project-skills 目录
            String projectRoot = System.getProperty("user.dir");
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load skills: " + e.getMessage());
        }
    }

    /**
     * 清除 AI 对话缓存 (Redis + 向量库)
     */
    @Operation(summary = "清除AI缓存")
    @Log(title = "智能会话", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> cleanHistory(@RequestParam(required = false) Long targetUserId) {
        Long currentUserId = SecurityUtils.getUserId();
        if (targetUserId != null && !currentUserId.equals(targetUserId)) {
            if (!SecurityUtils.isAdmin()) {
                return R.fail("无权操作其他用户的缓存");
            }
        } else {
            targetUserId = currentUserId;
        }

        String redisKey = HISTORY_KEY_PREFIX + targetUserId;
        stringRedisTemplate.delete(redisKey);

        try {
            JSONObject body = new JSONObject();
            body.set("user_id", String.valueOf(targetUserId));
            HttpRequest.post(MEMORY_DELETE_URL).timeout(5000).body(body.toString()).execute();
        } catch (Exception e) {
            System.err.println("Vector DB delete failed: " + e.getMessage());
        }

        return R.ok();
    }

    /**
     * 获取历史记录
     */
    @Operation(summary = "获取历史记录")
    @GetMapping("/history")
    public R<List<JSONObject>> getHistory() {
        Long userId = SecurityUtils.getUserId();
        String key = HISTORY_KEY_PREFIX + userId;
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
     * 清空历史记录
     */
    @Operation(summary = "清空历史记录")
    @Log(title = "智能会话", businessType = BusinessType.CLEAN)
    @DeleteMapping("/history/clean")
    public R<Void> cleanHistory() {
        Long userId = SecurityUtils.getUserId();
        String key = HISTORY_KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
        return R.ok();
    }

    /**
     * 发送对话 (流式 Stream) - Agent 模式
     * 使用 SSE (Server-Sent Events) 技术，实现打字机效果。
     * 
     * 【Agent 工具调用流程】：
     * 1. 用户提问 → LLM（携带 tools 定义）
     * 2. LLM 决定是否调用工具 → 如需调用，返回 tool_calls
     * 3. 执行工具（调用 RAG 检索引擎）→ 获取代码上下文
     * 4. 将工具结果发送给 LLM → LLM 生成最终回答
     * 5. 流式输出给用户
     */
    @Operation(summary = "发送对话(流式)")
    @Log(title = "智能会话", businessType = BusinessType.OTHER)
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> params) {
        final SseEmitter emitter = new SseEmitter(0L);
        String content = (String) params.get("content");
        Long userId = SecurityUtils.getUserId();
        
        if (content == null || content.trim().isEmpty()) {
            try {
                emitter.send(new JSONObject().set("content", "内容不能为空").toString());
            } catch (Exception ignored) {
            }
            emitter.complete();
            return emitter;
        }

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            StringBuilder fullReply = new StringBuilder();
            
            try {
                // 1. 构建 Agent 模式的 System Prompt
                String systemPrompt = buildAgentSystemPrompt();
                
                // 2. 构建消息列表
                JSONArray messages = new JSONArray();
                messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
                
                // 3. 注入近期对话历史
                addRecentHistory(messages, userId);
                
                // 4. 添加用户消息
                messages.add(new JSONObject().set("role", "user").set("content", content));
                
                // 5. Agent 循环：仅允许 1 轮工具调用，以提高响应速度
                int maxToolCalls = 1;
                int toolCallCount = 0;
                boolean toolCallLimitReached = false;
                
                while (toolCallCount < maxToolCalls) {
                    JSONObject requestBody = new JSONObject();
                    requestBody.set("model", deepseekModel);
                    requestBody.set("stream", false);
                requestBody.set("messages", messages);
                requestBody.set("tools", this.agentTools);
                requestBody.set("tool_choice", "auto");
                    
                    System.out.println("[Agent] 发送请求到 LLM，当前工具调用轮次: " + (toolCallCount + 1));
                    
                    HttpResponse response = HttpRequest.post(deepseekApiUrl)
                            .timeout(90000)
                            .header("Authorization", "Bearer " + deepseekApiKey)
                            .header("Content-Type", "application/json")
                            .body(requestBody.toString())
                            .execute();
                    
                    if (!response.isOk()) {
                        emitter.send(new JSONObject().set("content", "AI 服务响应异常: " + response.getStatus()).toString());
                        emitter.complete();
                        return;
                    }
                    
                    JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                    if (jsonResponse.containsKey("error")) {
                        emitter.send(new JSONObject().set("content", "AI 服务错误: " + jsonResponse.getJSONObject("error").getStr("message")).toString());
                        emitter.complete();
                        return;
                    }
                    
                    JSONObject choice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                    JSONObject assistantMessage = choice.getJSONObject("message");
                    String finishReason = choice.getStr("finish_reason");
                    
                    // 检查是否需要调用工具
                    if ("tool_calls".equals(finishReason) && assistantMessage.containsKey("tool_calls")) {
                        JSONArray toolCalls = assistantMessage.getJSONArray("tool_calls");
                        System.out.println("[Agent] LLM 请求调用工具，数量: " + toolCalls.size());
                        
                        messages.add(assistantMessage);
                        
                        JSONObject toolCall = toolCalls.getJSONObject(0);
                        String toolCallId = toolCall.getStr("id");
                        JSONObject functionObj = toolCall.getJSONObject("function");
                        String functionName = functionObj.getStr("name");
                        String argumentsStr = functionObj.getStr("arguments");
                        
                        System.out.println("[Agent] 执行工具: " + functionName + ", 参数: " + argumentsStr);
                        
                        String toolResult = executeAgentTool(functionName, argumentsStr);
                        
                        JSONObject toolMessage = new JSONObject();
                        toolMessage.set("role", "tool");
                        toolMessage.set("tool_call_id", toolCallId);
                        toolMessage.set("content", toolResult);
                        messages.add(toolMessage);
                        
                        System.out.println("[Agent] 工具执行完成，结果长度: " + toolResult.length());
                    
                    toolCallCount++;
                    
                    // 检查是否达到工具调用上限
                    if (toolCallCount >= maxToolCalls) {
                        System.out.println("[Agent] 达到工具调用上限 (" + maxToolCalls + " 轮)，强制生成回答");
                        toolCallLimitReached = true;
                        break;
                    }
                    continue;
                }
                
                System.out.println("[Agent] LLM 准备生成最终回答，切换到流式输出");
                break;
            }
            
            // 关键修正：无论是否达到调用上限，在进入最终回答阶段前，都必须强制注入禁止指令
            // 只有当 messages 最后一条不是系统指令时才添加，避免重复
            JSONObject lastMsg = messages.getJSONObject(messages.size() - 1);
            if (!"system".equals(lastMsg.getStr("role")) || !lastMsg.getStr("content").contains("禁止输出 <|DSML|")) {
                messages.add(new JSONObject()
                    .set("role", "system")
                    .set("content", "【系统指令】工具调用阶段已彻底结束（Do NOT search again）。\n" +
                                    "1. 忽略之前所有的 tools 定义，现在你没有可用的工具。\n" +
                                    "2. 严禁输出 '让我搜索...' 或 '我将查找...' 等尝试调用工具的语句。\n" +
                                    "3. 严禁输出 <|DSML| 或 function_calls 标签。\n" +
                                    "4. 请直接根据上方提供的上下文代码回答用户问题。如果上下文信息不足，请直接说明“信息不足”，不要尝试再次搜索。\n" +
                                    "5. 请立即开始生成回答的正文。"));
            }
            
            // 6. 流式输出最终回答
            // 策略调整：保留 tools 定义但强制 tool_choice="none"，避免模型因上下文结构不一致而困惑
            // 同时移除 stop 参数，防止误杀正常输出
            JSONObject streamRequest = new JSONObject();
            streamRequest.set("model", deepseekModel);
            streamRequest.set("stream", true);
            streamRequest.set("messages", messages);
            streamRequest.set("tools", this.agentTools);
            streamRequest.set("tool_choice", "none"); // 强制不调用工具
            
            System.out.println("[Agent] 开始生成最终回答 (Stream Mode), tool_choice=none");
                
                try (HttpResponse streamResponse = HttpRequest.post(deepseekApiUrl)
                        .timeout(300000) // 延长超时时间到 5分钟，避免长文本生成中断
                        .header("Authorization", "Bearer " + deepseekApiKey)
                        .header("Content-Type", "application/json")
                        .body(streamRequest.toString())
                        .execute(true)) {
                    
                    if (!streamResponse.isOk()) {
                        System.err.println("[Agent] AI 服务响应异常: " + streamResponse.getStatus());
                        emitter.send(new JSONObject().set("content", "AI 服务响应异常: " + streamResponse.getStatus()).toString());
                        emitter.complete();
                        return;
                    }
                    
                    try (InputStream inputStream = streamResponse.bodyStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty() || !line.startsWith("data:")) {
                                continue;
                            }
                            String data = line.substring(5).trim();
                            if (data.isEmpty() || "[DONE]".equals(data)) {
                                continue;
                            }
                            
                            JSONObject json = JSONUtil.parseObj(data);
                            if (json.containsKey("error")) {
                                String errorMsg = json.getStr("error");
                                System.err.println("[Agent] AI 服务返回错误: " + errorMsg);
                                emitter.send(new JSONObject().set("content", errorMsg).toString());
                                break;
                            }
                            
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices != null && !choices.isEmpty()) {
                                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String chunk = delta.getStr("content");
                                    if (chunk != null) {
                                        fullReply.append(chunk);
                                        emitter.send(new JSONObject().set("content", chunk).toString());
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 7. 存储对话历史
                if (fullReply.length() > 0) {
                    saveConversationHistory(userId, content, fullReply.toString(), startTime);
                    System.out.println("[Agent] 流式响应正常结束，回复长度: " + fullReply.length());
                } else {
                    System.out.println("[Agent] 流式响应结束，但回复为空");
                }
                
                emitter.send("[DONE]");
                emitter.complete();
                
            } catch (Exception e) {
                System.err.println("[Agent] 流式响应异常: " + e.getMessage());
                e.printStackTrace();
                try {
                    // 发送错误详情，前端已做兼容，不会覆盖已有内容
                    emitter.send(new JSONObject().set("content", "AI 服务调用失败: " + e.getMessage()).toString());
                } catch (Exception ignored) {
                }
                emitter.complete();
            }
        }).start();
        
        return emitter;
    }
    
    /**
     * 构建 Agent 模式的 System Prompt
     */
    private String buildAgentSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。\n\n");
        
        if (StrUtil.isNotEmpty(skillsContext)) {
            prompt.append(skillsContext).append("\n\n");
        }
        
        if (StrUtil.isNotEmpty(agentRuleContext)) {
            prompt.append(agentRuleContext);
        } else {
            prompt.append("## 输出行为约束\n");
            prompt.append("1. **严禁直接粘贴代码**：除非用户明确说看代码或写代码，否则将代码逻辑翻译为自然语言的业务规则。\n");
            prompt.append("2. **识别关键模式**：\n");
            prompt.append("   - 看到 `LEFT JOIN` → 解释为查询时自动关联了XX信息\n");
            prompt.append("   - 看到 `if (isAdmin)` 或权限注解 → 解释为管理员和普通用户的权限差异\n");
            prompt.append("   - 看到 `del_flag = 0` → 解释为默认过滤已删除数据\n");
            prompt.append("   - 看到 `@DataScope` → 解释为数据权限控制，用户只能看到自己权限范围内的数据\n");
            prompt.append("3. **引用而非堆砌**：可以提及关键类名（如 `SysProjectService`），但不展开实现代码。\n");
            prompt.append("4. **直接回答问题**：不要说根据代码上下文...、根据检索结果...这类铺垫的话。\n");
            prompt.append("5. **避免重复搜索**：每次工具调用都会返回最相关的 10 个代码片段。请充分利用这些上下文，尽量一次性解决问题，除非检索结果完全不相关，否则不要反复调用搜索工具。\n");
            prompt.append("6. **禁止输出 DSML**：严禁在回答中包含 <|DSML|... 等标签。\n");
            prompt.append("7. **格式规范**：使用标准的 Markdown 格式输出。\n");
        }
        
        return prompt.toString();
    }
    
    /**
     * 执行 Agent 工具
     */
    private String executeAgentTool(String functionName, String argumentsJson) {
        try {
            if ("search_codebase".equals(functionName)) {
                JSONObject args = JSONUtil.parseObj(argumentsJson);
                String query = args.getStr("query");
                
                if (StrUtil.isBlank(query)) {
                    return "错误：搜索关键词不能为空";
                }
                
                JSONObject ragRequest = new JSONObject();
                ragRequest.set("question", query);
                ragRequest.set("n_results", 10);
                
                long ragStartTime = System.currentTimeMillis();
                System.out.println("[Agent Tool] 调用 RAG 引擎: " + RAG_API_URL + ", query=" + query);
                
                String ragResponse = HttpRequest.post(RAG_API_URL)
                        .timeout(60000) // 延长超时时间到 60秒，应对大量数据检索
                        .body(ragRequest.toString())
                        .execute()
                        .body();
                
                System.out.println("[Agent Tool] RAG 响应耗时: " + (System.currentTimeMillis() - ragStartTime) + "ms");
                
                JSONObject ragJson = JSONUtil.parseObj(ragResponse);
                JSONArray results = ragJson.getJSONArray("results");
                
                if (results == null || results.isEmpty()) {
                    return "未找到与 \"" + query + "\" 相关的代码。请尝试使用更具体的类名、方法名或功能描述。";
                }
                
                StringBuilder sb = new StringBuilder();
                sb.append("找到 ").append(results.size()).append(" 个相关代码片段：\n\n");
                
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    JSONObject meta = item.getJSONObject("metadata");
                    String codeContent = item.getStr("content");
                    String source = meta != null ? meta.getStr("source") : "unknown";
                    String name = meta != null ? meta.getStr("name") : "";
                    String type = meta != null ? meta.getStr("type") : "";
                    
                    sb.append("### 代码片段 ").append(i + 1);
                    if (StrUtil.isNotBlank(name)) {
                        sb.append(" - ").append(name);
                    }
                    if (StrUtil.isNotBlank(type)) {
                        sb.append(" (").append(type).append(")");
                    }
                    sb.append("\n");
                    sb.append("**来源**: ").append(source).append("\n");
                    sb.append("```java\n").append(codeContent).append("\n```\n\n");
                }
                
                sb.append("\n【系统提示】检索已完成。以上代码已包含足够信息，请根据这些内容直接回答用户问题，不要再次调用工具。");
                
                return sb.toString();
            }
            
            return "未知工具: " + functionName;
            
        } catch (Exception e) {
            System.err.println("[Agent Tool] 工具执行失败: " + e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }
    
    /**
     * 从问题中提取主题关键词
     * 策略升级：优先调用 Python 引擎的 NLP 接口进行智能提取，失败则回退到本地规则匹配
     */
    private String extractTopic(String question) {
        if (StrUtil.isBlank(question)) {
            return "未知";
        }
        
        // 1. 尝试调用 Python NLP 接口
        try {
            JSONObject body = new JSONObject();
            body.set("text", question);
            
            // 短超时，避免阻塞太久 (300ms)
            String result = HttpRequest.post(NLP_TOPIC_URL)
                    .timeout(300)
                    .body(body.toString())
                    .execute()
                    .body();
            
            if (StrUtil.isNotEmpty(result)) {
                JSONObject json = JSONUtil.parseObj(result);
                String topic = json.getStr("topic");
                if (StrUtil.isNotBlank(topic)) {
                    System.out.println("NLP Topic Extracted: " + topic + " (from: " + question + ")");
                    return topic;
                }
            }
        } catch (Exception e) {
            // NLP 服务未启动或超时，降级到本地规则
            // System.err.println("NLP Topic extraction failed, fallback to local rules.");
        }
        
        // 2. 降级策略：本地规则匹配 (保留原有逻辑作为兜底)
        String[] keywords = {
            // 核心技术栈
            "Spring Boot", "Spring Security", "MyBatis", "Redis", "Vue", "Element Plus", "TypeScript", "Vite", "Pinia", "Axios",
            "Python", "FastAPI", "Chroma", "Watchdog", "Java", "Maven", "Gradle", "MySQL", "Logback",
            // AI 相关
            "RAG", "向量", "DeepSeek", "Gemini", "LLM", "SSE", "流式", "Embedding", "Token", "Context", "Prompt", "Agent",
            // 系统模块
            "权限", "菜单", "角色", "用户", "部门", "日志", "字典", "代码生成", "定时任务", "系统监控", "服务监控",
            // 开发运维
            "启动", "部署", "配置", "环境", "调试", "报错", "异常", "Bug", "接口", "API"
        };
        
        String lowerQuestion = question.toLowerCase();
        for (String keyword : keywords) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        
        return null; 
    }

    /**
     * 保存对话历史（Redis + 向量库 + MySQL）
     */
    private void saveConversationHistory(Long userId, String question, String answer, long startTime) {
        long now = System.currentTimeMillis();
        
        // 1. 存入 Redis 短期历史
        JSONObject userRecord = new JSONObject().set("role", "user").set("content", question);
        JSONObject aiRecord = new JSONObject().set("role", "assistant").set("content", answer);
        String key = HISTORY_KEY_PREFIX + userId;
        try {
            stringRedisTemplate.opsForList().rightPush(key, userRecord.toString());
            stringRedisTemplate.opsForList().rightPush(key, aiRecord.toString());
            if (stringRedisTemplate.opsForList().size(key) > 20) {
                stringRedisTemplate.opsForList().trim(key, -20, -1);
            }
            stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("Redis save failed: " + e.getMessage());
        }

        // 2. 存入 Vector DB 长期记忆
        try {
            JSONObject memoryAdd = new JSONObject();
            memoryAdd.set("user_id", String.valueOf(userId));
            JSONArray memoryMessages = new JSONArray();
            memoryMessages.add(new JSONObject().set("content", question).set("role", "user").set("timestamp", now).set("sequence", now));
            memoryMessages.add(new JSONObject().set("content", answer).set("role", "assistant").set("timestamp", now + 1).set("sequence", now + 1));
            memoryAdd.set("messages", memoryMessages);
            HttpRequest.post(MEMORY_ADD_URL).timeout(5000).body(memoryAdd.toString()).execute();
        } catch (Exception e) {
            System.err.println("Vector DB save failed: " + e.getMessage());
        }
        
        // 3. 持久化到 MySQL
        try {
            long duration = System.currentTimeMillis() - startTime;
            SysAiSession aiSession = new SysAiSession();
            aiSession.setUserId(userId);
            aiSession.setQuestion(question);
            aiSession.setAnswer(answer);
            aiSession.setModel(deepseekModel);
            int tokens = (question.length() + answer.length()) * 2;
            aiSession.setTokens(tokens);
            aiSession.setDuration((int) duration);
            
            // 提取并保存主题
            String topic = extractTopic(question);
            if (topic != null) {
                aiSession.setTopic(topic);
            }
            
            aiSessionService.save(aiSession);
        } catch (Exception e) {
            System.err.println("Failed to save ai session: " + e.getMessage());
        }
    }

    /**
     * 判断是否需要检索向量库（保留作为降级方案）
     */
    private boolean shouldQueryVector(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        String text = content.toLowerCase();
        String[] keywords = new String[]{
                "接口", "api", "controller", "service", "mapper", "sql", "数据库", "表", "字段", "报错", "异常", "堆栈",
                "代码", "类", "方法", "函数", "bug", "日志", "运行", "编译", "构建", "依赖", "配置", "yml",
                "刚才", "上次", "之前", "继续", "前面", "我们聊过", "你说过", "历史", "上下文"
        };
        return StrUtil.containsAny(text, keywords);
    }

    /**
     * 将 Redis 中的近期对话历史添加到消息列表中
     */
    private void addRecentHistory(JSONArray messages, Long userId) {
        try {
            String key = HISTORY_KEY_PREFIX + userId;
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
