package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.swiftboot.admin.domain.dto.SysAiConfigDTO;
import com.swiftboot.admin.domain.vo.SysAiConfigVO;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.web.bind.annotation.*;

/**
 * AI 配置控制器
 * 提供 AI 配置的查询和更新功能
 */
@Tag(name = "AI配置管理")
@RestController
@RequestMapping("/system/ai/config")
public class SysAiConfigController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysAiController sysAiController;

    /**
     * Redis Key
     */
    private static final String AI_CONFIG_KEY = "ai:config";

    /**
     * 各提供商的 API URL
     */
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
    // MiniMax Anthropic 兼容接口地址 (推荐使用，支持 native thinking)
    private static final String MINIMAX_API_URL = "https://api.minimaxi.com/anthropic/v1/messages";

    /**
     * 各提供商的可用模型
     */
    private static final String[] DEEPSEEK_MODELS = {"deepseek-chat", "deepseek-coder"};
    private static final String[] GEMINI_MODELS = {"gemini-2.0-flash", "gemini-1.5-pro"};
    private static final String[] MINIMAX_MODELS = {
        "MiniMax-M2.7", 
        "MiniMax-M2.7-highspeed", 
        "MiniMax-M2.5", 
        "MiniMax-M2.5-highspeed", 
        "MiniMax-M2.1", 
        "MiniMax-M2.1-highspeed", 
        "MiniMax-M2"
    };

    /**
     * 获取当前 AI 配置
     * 优先从 Redis 读取，无则返回默认值
     */
    @Operation(summary = "获取AI配置")
    @GetMapping
    @SaCheckPermission("tool:config:list")
    public R<SysAiConfigVO> getConfig() {
        SysAiConfigVO vo = new SysAiConfigVO();
        
        // 尝试从 Redis 获取配置
        String configJson = stringRedisTemplate.opsForValue().get(AI_CONFIG_KEY);
        
        if (StrUtil.isNotEmpty(configJson)) {
            // 从 Redis 读取配置
            JSONObject config = JSONUtil.parseObj(configJson);
            vo.setProvider(config.getStr("provider"));
            vo.setModel(config.getStr("model"));
            vo.setApiKey(maskApiKey(config.getStr("apiKey")));
            vo.setApiUrl(config.getStr("apiUrl"));
            vo.setAvailableModels(getAvailableModels(vo.getProvider()));
        } else {
            // 返回空配置，让前端知道可以设置
            vo.setProvider("deepseek");
            vo.setApiUrl(DEEPSEEK_API_URL);
            vo.setAvailableModels(DEEPSEEK_MODELS);
        }
        
        return R.ok(vo);
    }

    /**
     * 更新 AI 配置
     * 保存到 Redis，Key 为 ai:config
     */
    @Operation(summary = "更新AI配置")
    @Log(title = "AI配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @SaCheckPermission("tool:config:edit")
    public R<Void> updateConfig(@Valid @RequestBody SysAiConfigDTO dto) {
        // 构建配置对象
        JSONObject config = new JSONObject();
        config.set("provider", dto.getProvider());
        config.set("model", dto.getModel());
        config.set("apiKey", dto.getApiKey());
        
        // 只有当传入的 apiUrl 为空时，才使用默认 apiUrl
        String apiUrl = dto.getApiUrl();
        if (StrUtil.isBlank(apiUrl)) {
            apiUrl = getApiUrl(dto.getProvider());
        }
        config.set("apiUrl", apiUrl);
        
        // 保存到 Redis
        stringRedisTemplate.opsForValue().set(AI_CONFIG_KEY, config.toString());
        
        // 通知 SysAiController 刷新配置
        sysAiController.refreshAiConfig();
        
        return R.ok();
    }

    /**
     * 测试 AI 模型连通性
     */
    @Operation(summary = "测试模型连通性")
    @PostMapping("/test-connection")
    @SaCheckPermission("tool:config:edit")
    public R<String> testConnection(@Valid @RequestBody SysAiConfigDTO dto) {
        String apiUrl = dto.getApiUrl();
        if (StrUtil.isBlank(apiUrl)) {
            apiUrl = getApiUrl(dto.getProvider());
        }
        
        boolean isAnthropic = apiUrl.contains("/anthropic/");
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", dto.getModel());
        requestBody.set("stream", false);
        
        if (isAnthropic) {
            requestBody.set("max_tokens", 100);
            requestBody.set("messages", new cn.hutool.json.JSONArray().put(new JSONObject().set("role", "user").set("content", "Hello!")));
        } else {
            requestBody.set("messages", new cn.hutool.json.JSONArray().put(new JSONObject().set("role", "user").set("content", "Hello!")));
        }
        
        try (HttpResponse response = HttpRequest.post(apiUrl)
                .timeout(10000)
                .header("Authorization", "Bearer " + dto.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(requestBody.toString())
                .execute()) {
                
            if (response.isOk()) {
                return R.ok("连接成功！模型响应正常。");
            } else {
                throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                        "连接失败：HTTP 状态码 " + response.getStatus() + "\n" + response.body());
            }
        } catch (Exception e) {
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "请求异常：" + e.getMessage());
        }
    }

    /**
     * 根据提供商获取 API URL
     */
    private String getApiUrl(String provider) {
        if (provider == null) {
            return DEEPSEEK_API_URL;
        }
        return switch (provider.toLowerCase()) {
            case "deepseek" -> DEEPSEEK_API_URL;
            case "gemini" -> GEMINI_API_URL;
            case "minimax" -> MINIMAX_API_URL;
            default -> DEEPSEEK_API_URL;
        };
    }

    /**
     * 获取提供商的可用模型列表
     */
    private String[] getAvailableModels(String provider) {
        if (provider == null) {
            return DEEPSEEK_MODELS;
        }
        return switch (provider.toLowerCase()) {
            case "deepseek" -> DEEPSEEK_MODELS;
            case "gemini" -> GEMINI_MODELS;
            case "minimax" -> MINIMAX_MODELS;
            default -> DEEPSEEK_MODELS;
        };
    }

    /**
     * 脱敏显示 API Key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
