package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI block word category entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_block_category")
@Schema(description = "AI block word category")
public class SysAiBlockCategory extends BaseEntity {

    @Schema(description = "Linked dict data id")
    private Long dictDataId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Category code")
    private String categoryCode;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Status (0 enabled, 1 disabled)")
    private Integer status;
}
