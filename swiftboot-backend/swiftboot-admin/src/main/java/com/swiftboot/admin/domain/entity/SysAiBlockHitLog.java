package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI block word hit log entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_block_hit_log")
@Schema(description = "AI block word hit log")
public class SysAiBlockHitLog extends BaseEntity {

    @Schema(description = "User id")
    private Long userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Category id")
    private Long categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Word text")
    private String wordText;

    @Schema(description = "Question content")
    private String questionContent;

    @Schema(description = "Login ip")
    private String loginIp;
}
