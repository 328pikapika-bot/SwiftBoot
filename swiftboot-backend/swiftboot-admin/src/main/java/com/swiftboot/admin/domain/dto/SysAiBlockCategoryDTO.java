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
 * AI block category save DTO.
 */
@Data
@Schema(description = "AI block category save DTO")
public class SysAiBlockCategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Category id")
    private Long id;

    @Schema(description = "Linked dict data id")
    private Long dictDataId;

    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 40, message = "Category name length cannot exceed 40")
    @Schema(description = "Category name")
    private String categoryName;

    @NotBlank(message = "Category code cannot be empty")
    @Size(max = 40, message = "Category code length cannot exceed 40")
    @Schema(description = "Category code")
    private String categoryCode;

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
