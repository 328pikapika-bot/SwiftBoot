package com.swiftboot.admin.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 配置响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI配置响应")
public class SysAiConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "AI提供商(deepseek/gemini/minimax)")
    private String provider;

    @Schema(description = "模型名称")
    private String model;

    @Schema(description = "API Key(脱敏显示)")
    private String apiKey;

    @Schema(description = "API URL")
    private String apiUrl;

    @Schema(description = "可用模型列表")
    private String[] availableModels;
}
