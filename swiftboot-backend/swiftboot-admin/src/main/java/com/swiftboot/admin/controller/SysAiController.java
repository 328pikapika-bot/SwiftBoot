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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 智能助手控制器
 */
@Tag(name = "AI助手")
@RestController
@RequestMapping("/system/ai")
public class SysAiController {

    @Value("${ai.provider:deepseek}")
    private String provider;

    @Value("${ai.deepseek.api-url:}")
    private String deepseekApiUrl;

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.model:}")
    private String deepseekModel;

    @Value("${ai.gemini.api-url:}")
    private String geminiApiUrl;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:}")
    private String geminiModel;

    // 缓存 Skills 内容
    private String skillsContext = "";
    
    // Python 检索引擎地址 (RAG)
    private static final String RAG_API_URL = "http://localhost:8001/retrieve";

    @PostConstruct
    public void initSkills() {
        try {
            // 扫描项目根目录下的 project-skills 目录
            // 注意：实际部署时路径可能需要调整，这里假设是本地开发环境
            String projectRoot = System.getProperty("user.dir");
            // 如果是模块运行，可能需要回退一级目录
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
                            // 简单的 frontmatter 解析
                            String name = folder.getName();
                            // 移除 frontmatter
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

    @Operation(summary = "发送对话")
    @PostMapping("/chat")
    public R<String> chat(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return R.fail("内容不能为空");
        }

        String tempSystemPrompt = "你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。请用简洁、专业的语言回答用户关于开发、代码或项目管理的问题。";
        if (StrUtil.isNotEmpty(skillsContext)) {
            tempSystemPrompt += "\n\n" + skillsContext;
        }
        final String systemPrompt = tempSystemPrompt;

        try {
            HttpResponse response;
            if ("gemini".equalsIgnoreCase(provider)) {
                JSONObject requestBody = new JSONObject();
                JSONObject systemInstruction = new JSONObject();
                systemInstruction.set("role", "system");
                systemInstruction.set("parts", new JSONArray().add(new JSONObject().set("text", systemPrompt)));
                requestBody.set("systemInstruction", systemInstruction);

                JSONObject userMessage = new JSONObject();
                userMessage.set("role", "user");
                userMessage.set("parts", new JSONArray().add(new JSONObject().set("text", content)));
                requestBody.set("contents", new JSONArray().add(userMessage));

                String finalUrl = geminiApiUrl;
                if (StrUtil.isNotEmpty(geminiApiKey) && !finalUrl.contains("key=")) {
                    finalUrl = finalUrl + (finalUrl.contains("?") ? "&" : "?") + "key=" + geminiApiKey;
                }
                System.out.println("Sending AI request to: " + finalUrl);
                response = HttpRequest.post(finalUrl)
                        .timeout(90000)
                        .header("Content-Type", "application/json")
                        .body(requestBody.toString())
                        .execute();
            } else {
                JSONObject requestBody = new JSONObject();
                requestBody.set("model", deepseekModel);
                requestBody.set("stream", false);

                JSONArray messages = new JSONArray();
                JSONObject systemMessage = new JSONObject();
                systemMessage.set("role", "system");
                systemMessage.set("content", systemPrompt);
                messages.add(systemMessage);

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
            }

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                if (jsonResponse.containsKey("error")) {
                     return R.fail("AI 服务错误: " + jsonResponse.getJSONObject("error").getStr("message"));
                }
                String reply;
                if ("gemini".equalsIgnoreCase(provider)) {
                    reply = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getStr("text");
                } else {
                    reply = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getStr("content");
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

    @Operation(summary = "发送对话(流式)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, String> params) {
        final SseEmitter emitter = new SseEmitter(0L);
        String content = params.get("content");
        if (content == null || content.trim().isEmpty()) {
            try {
                JSONObject msg = new JSONObject().set("content", "内容不能为空");
                emitter.send(msg.toString());
            } catch (Exception ignored) {
            }
            emitter.complete();
            return emitter;
        }

        new Thread(() -> {
            try {
                // 1. 调用 Python RAG 引擎检索相关上下文
                String ragContext = "";
                try {
                    JSONObject ragRequest = new JSONObject();
                    ragRequest.set("question", content);
                    ragRequest.set("n_results", 3);
                    
                    System.out.println("Calling RAG Engine: " + RAG_API_URL);
                    String ragResponse = HttpRequest.post(RAG_API_URL)
                            .timeout(5000) // 5秒超时
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
                            
                            sb.append("--- Source: ").append(meta.getStr("source")).append(" ---\n");
                            sb.append(codeContent).append("\n");
                        }
                        sb.append("=========================\n");
                        ragContext = sb.toString();
                        System.out.println("RAG Context found, length: " + ragContext.length());
                    }
                } catch (Exception e) {
                    System.err.println("RAG Engine call failed: " + e.getMessage());
                    // 检索失败不影响主流程
                }

                // 2. 组装 System Prompt
                String tempSystemPrompt = "你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。请用简洁、专业的语言回答用户关于开发、代码或项目管理的问题。";
                
                // 添加 Skills
                if (StrUtil.isNotEmpty(skillsContext)) {
                    tempSystemPrompt += "\n\n" + skillsContext;
                }
                
                // 添加 RAG 上下文
                if (StrUtil.isNotEmpty(ragContext)) {
                    tempSystemPrompt += ragContext;
                    tempSystemPrompt += "\n\n请优先根据上述【参考项目代码上下文】来回答用户的问题。如果上下文中没有相关信息，再根据你的通用知识回答。";
                }

                final String systemPrompt = tempSystemPrompt;

                if ("gemini".equalsIgnoreCase(provider)) {
                    streamGemini(emitter, systemPrompt, content);
                    return;
                }

                JSONObject requestBody = new JSONObject();
                requestBody.set("model", deepseekModel);
                requestBody.set("stream", true);

                JSONArray messages = new JSONArray();
                JSONObject systemMessage = new JSONObject();
                systemMessage.set("role", "system");
                systemMessage.set("content", systemPrompt);
                messages.add(systemMessage);

                JSONObject userMessage = new JSONObject();
                userMessage.set("role", "user");
                userMessage.set("content", content);
                messages.add(userMessage);

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
                                JSONObject msg = new JSONObject().set("content", "AI 服务错误: " + json.getJSONObject("error").getStr("message"));
                                emitter.send(msg.toString());
                                break;
                            }
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices == null || choices.isEmpty()) {
                                continue;
                            }
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject delta = choice.getJSONObject("delta");
                            String chunk = "";
                            if (delta != null) {
                                chunk = delta.getStr("content");
                            } else {
                                JSONObject message = choice.getJSONObject("message");
                                if (message != null) {
                                    chunk = message.getStr("content");
                                }
                            }
                            if (StrUtil.isNotEmpty(chunk)) {
                                JSONObject msg = new JSONObject().set("content", chunk);
                                emitter.send(msg.toString());
                            }
                        }
                    }
                    emitter.send("[DONE]");
                    emitter.complete();
                }
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

    private void streamGemini(SseEmitter emitter, String systemPrompt, String content) {
        JSONObject requestBody = new JSONObject();
        JSONObject systemInstruction = new JSONObject();
        systemInstruction.set("role", "system");
        systemInstruction.set("parts", new JSONArray().add(new JSONObject().set("text", systemPrompt)));
        requestBody.set("systemInstruction", systemInstruction);

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("parts", new JSONArray().add(new JSONObject().set("text", content)));
        requestBody.set("contents", new JSONArray().add(userMessage));

        String finalUrl = geminiApiUrl.replace(":generateContent", ":streamGenerateContent");
        if (!finalUrl.contains("streamGenerateContent")) {
             if (!finalUrl.endsWith("/")) finalUrl += ":";
             finalUrl += "streamGenerateContent";
        }
        finalUrl += (finalUrl.contains("?") ? "&" : "?") + "alt=sse";

        if (StrUtil.isNotEmpty(geminiApiKey) && !finalUrl.contains("key=")) {
            finalUrl = finalUrl + "&key=" + geminiApiKey;
        }

        try (HttpResponse response = HttpRequest.post(finalUrl)
                .timeout(90000)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute(true)) {
            
            if (!response.isOk()) {
                JSONObject msg = new JSONObject().set("content", "AI 服务响应异常: " + response.getStatus());
                emitter.send(msg.toString());
                emitter.complete();
                return;
            }

            try (InputStream inputStream = response.bodyStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty()) continue;
                    
                    JSONObject json = JSONUtil.parseObj(data);
                    if (json.containsKey("candidates")) {
                         JSONArray candidates = json.getJSONArray("candidates");
                         if (!candidates.isEmpty()) {
                             JSONObject candidate = candidates.getJSONObject(0);
                             JSONObject contentObj = candidate.getJSONObject("content");
                             if (contentObj != null) {
                                 JSONArray parts = contentObj.getJSONArray("parts");
                                 if (parts != null && !parts.isEmpty()) {
                                     String text = parts.getJSONObject(0).getStr("text");
                                     if (StrUtil.isNotEmpty(text)) {
                                         JSONObject msg = new JSONObject().set("content", text);
                                         emitter.send(msg.toString());
                                     }
                                 }
                             }
                         }
                    }
                }
                emitter.send("[DONE]");
                emitter.complete();
            }
        } catch (Exception e) {
             try {
                JSONObject msg = new JSONObject().set("content", "AI 服务调用失败: " + e.getMessage());
                emitter.send(msg.toString());
            } catch (Exception ignored) {}
            emitter.complete();
        }
    }

    private String callGemini(String systemPrompt, String content) {
        JSONObject requestBody = new JSONObject();
        JSONObject systemInstruction = new JSONObject();
        systemInstruction.set("role", "system");
        systemInstruction.set("parts", new JSONArray().add(new JSONObject().set("text", systemPrompt)));
        requestBody.set("systemInstruction", systemInstruction);

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("parts", new JSONArray().add(new JSONObject().set("text", content)));
        requestBody.set("contents", new JSONArray().add(userMessage));

        String finalUrl = geminiApiUrl;
        if (StrUtil.isNotEmpty(geminiApiKey) && !finalUrl.contains("key=")) {
            finalUrl = finalUrl + (finalUrl.contains("?") ? "&" : "?") + "key=" + geminiApiKey;
        }
        try (HttpResponse response = HttpRequest.post(finalUrl)
                .timeout(90000)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute()) {
            if (!response.isOk()) {
                return "AI 服务响应异常: " + response.getStatus();
            }
            JSONObject jsonResponse = JSONUtil.parseObj(response.body());
            if (jsonResponse.containsKey("error")) {
                return "AI 服务错误: " + jsonResponse.getJSONObject("error").getStr("message");
            }
            return jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getStr("text");
        }
    }
}
