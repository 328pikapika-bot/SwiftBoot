package com.swiftboot.admin.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI block word view object.
 */
@Data
@Schema(description = "AI block word view object")
public class SysAiBlockWordVO {

    @Schema(description = "Word id")
    private Long id;

    @Schema(description = "Category id")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Word text")
    private String wordText;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Remark")
    private String remark;
}
