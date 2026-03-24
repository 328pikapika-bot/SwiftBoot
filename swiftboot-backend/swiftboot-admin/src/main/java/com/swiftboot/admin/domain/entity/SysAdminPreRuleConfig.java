package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Administrator safety pre-rule config entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_ai_admin_pre_rule_config")
@Schema(description = "Administrator safety pre-rule config")
public class SysAdminPreRuleConfig extends BaseEntity {

    @Schema(description = "Config key")
    private String configKey;

    @Schema(description = "Whether enabled")
    private Boolean enabled;

    @Schema(description = "Interception message")
    private String interceptionMessage;

    @Schema(description = "Rules json")
    private String rulesJson;
}
