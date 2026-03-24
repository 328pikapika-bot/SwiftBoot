package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator pre-check safety rules configuration.
 */
@Data
@Schema(description = "Administrator pre-check safety rules configuration")
public class SysAdminPreRuleConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Whether administrator pre-check is enabled")
    private Boolean enabled = Boolean.TRUE;

    @Size(max = 120, message = "interceptionMessage length cannot exceed 120")
    @Schema(description = "Unified interception message")
    private String interceptionMessage;

    @Valid
    @Size(max = 10, message = "At most 10 rules are allowed")
    @Schema(description = "Rule list")
    private List<RuleItem> rules = new ArrayList<>();

    @Data
    @Schema(description = "Administrator rule item")
    public static class RuleItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Size(max = 64, message = "id length cannot exceed 64")
        @Schema(description = "Rule id")
        private String id;

        @NotBlank(message = "ruleName is required")
        @Size(max = 40, message = "ruleName length cannot exceed 40")
        @Schema(description = "Rule name")
        private String ruleName;

        @NotBlank(message = "ruleContent is required")
        @Size(max = 200, message = "ruleContent length cannot exceed 200")
        @Schema(description = "Rule content. One keyword per line, use regex: prefix for regex rules")
        private String ruleContent;

        @Schema(description = "Whether the rule is enabled")
        private Boolean enabled = Boolean.TRUE;

        @Min(value = 0, message = "priority cannot be less than 0")
        @Max(value = 999, message = "priority cannot exceed 999")
        @Schema(description = "Priority, larger value executes earlier")
        private Integer priority = 100;
    }
}
