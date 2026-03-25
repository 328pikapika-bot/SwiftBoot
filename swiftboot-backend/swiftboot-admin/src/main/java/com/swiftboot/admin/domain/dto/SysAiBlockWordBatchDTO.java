package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI block word batch save DTO.
 */
@Data
@Schema(description = "AI block word batch save DTO")
public class SysAiBlockWordBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Category cannot be empty")
    @Schema(description = "Category id")
    private Long categoryId;

    @NotBlank(message = "Word lines cannot be empty")
    @Size(max = 4000, message = "Word lines are too long")
    @Schema(description = "One block word per line")
    private String wordLines;

    @Size(max = 200, message = "Remark length cannot exceed 200")
    @Schema(description = "Remark")
    private String remark;
}
