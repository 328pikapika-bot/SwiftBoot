package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI block word entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_block_word")
@Schema(description = "AI block word")
public class SysAiBlockWord extends BaseEntity {

    @Schema(description = "Category id")
    private Long categoryId;

    @Schema(description = "Word text")
    private String wordText;

    @Schema(description = "Match type (contains/exact)")
    private String matchType;

    @Schema(description = "Sort")
    private Integer sort;

    @Schema(description = "Status (0 enabled, 1 disabled)")
    private Integer status;
}
