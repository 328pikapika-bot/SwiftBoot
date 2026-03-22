package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * File list query DTO.
 */
@Data
@Schema(description = "File query")
public class SysFileQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Business type")
    private String bizType;

    @Schema(description = "Business id")
    private Long bizId;

    @Schema(description = "Storage type")
    private String storageType;
}
