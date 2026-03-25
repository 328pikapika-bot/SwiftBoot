package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI block word save DTO.
 */
@Data
@Schema(description = "AI block word save DTO")
public class SysAiBlockWordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Word id")
    private Long id;

    @NotNull(message = "Category cannot be empty")
    @Schema(description = "Category id")
    private Long categoryId;

    @NotBlank(message = "Word text cannot be empty")
    @Size(max = 80, message = "Word text length cannot exceed 80")
    @Schema(description = "Word text")
    private String wordText;

    @NotNull(message = "Status cannot be empty")
    @Schema(description = "Status (0 enabled, 1 disabled)")
    private Integer status;

    @Min(value = 0, message = "Sort cannot be less than 0")
    @Max(value = 999, message = "Sort cannot exceed 999")
    @Schema(description = "Sort")
    private Integer sort = 100;

    @Size(max = 200, message = "Remark length cannot exceed 200")
    @Schema(description = "Remark")
    private String remark;
}
