package com.swiftboot.admin.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI block word overview view object.
 */
@Data
@Schema(description = "AI block word overview")
public class SysAiBlockOverviewVO {

    @Schema(description = "Enabled category count")
    private Integer enabledCategoryCount = 0;

    @Schema(description = "Enabled word count")
    private Integer enabledWordCount = 0;

    @Schema(description = "Total category count")
    private Integer totalCategoryCount = 0;

    @Schema(description = "Total word count")
    private Integer totalWordCount = 0;

    @Schema(description = "Category list")
    private List<SysAiBlockCategoryVO> categories = new ArrayList<>();

    @Schema(description = "Word list")
    private List<SysAiBlockWordVO> words = new ArrayList<>();
}
