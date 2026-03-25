package com.swiftboot.admin.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI block category view object.
 */
@Data
@Schema(description = "AI block category view object")
public class SysAiBlockCategoryVO {

    @Schema(description = "Category id")
    private Long id;

    @Schema(description = "Linked dict data id")
    private Long dictDataId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Category code")
    private String categoryCode;

    @Schema(description = "Status")
    private Integer status;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Word count")
    private Integer wordCount;

    @Schema(description = "Preview words")
    private List<String> previewWords = new ArrayList<>();
}
