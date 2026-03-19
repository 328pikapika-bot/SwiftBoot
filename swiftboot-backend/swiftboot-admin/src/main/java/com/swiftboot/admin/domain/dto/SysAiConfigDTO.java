package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 配置请求 DTO
 */
@Data
@Schema(description = "AI配置请求")
public class SysAiConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "AI提供商不能为空")
    @Schema(description = "AI提供商(deepseek/gemini/minimax)", required = true)
    private String provider;

    @NotBlank(message = "模型不能为空")
    @Schema(description = "模型名称", required = true)
    private String model;

    @Schema(description = "API URL")
    private String apiUrl;

    @NotBlank(message = "API Key不能为空")
    @Schema(description = "API Key", required = true)
    private String apiKey;
}
