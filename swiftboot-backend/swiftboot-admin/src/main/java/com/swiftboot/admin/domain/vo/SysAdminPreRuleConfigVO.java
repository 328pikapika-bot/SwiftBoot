package com.swiftboot.admin.domain.vo;

import com.swiftboot.admin.domain.dto.SysAdminPreRuleConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Administrator pre-check safety rules configuration view object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Administrator pre-check safety rules configuration")
public class SysAdminPreRuleConfigVO extends SysAdminPreRuleConfigDTO {

    @Schema(description = "Maximum number of rules")
    private Integer maxRules = 10;

    @Schema(description = "Maximum length of each rule content")
    private Integer maxRuleLength = 200;
}
