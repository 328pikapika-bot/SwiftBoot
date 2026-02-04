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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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

    @PostConstruct
    public void initSkills() {
        try {
            // 扫描项目根目录下的 .trae/skills 目录
            // 注意：实际部署时路径可能需要调整，这里假设是本地开发环境
            String projectRoot = System.getProperty("user.dir");
            // 如果是模块运行，可能需要回退一级目录
            if (projectRoot.endsWith("swiftboot-admin")) {
                projectRoot = new File(projectRoot).getParentFile().getParent();
            } else if (projectRoot.endsWith("swiftboot-backend")) {
                projectRoot = new File(projectRoot).getParent();
            }
            
            File skillsDir = new File(projectRoot, ".trae/skills");
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

        String systemPrompt = "你是 SwiftBoot 的智能助手，一个专业的全栈开发专家。请用简洁、专业的语言回答用户关于开发、代码或项目管理的问题。";
        if (StrUtil.isNotEmpty(skillsContext)) {
            systemPrompt += "\n\n" + skillsContext;
        }

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
}
