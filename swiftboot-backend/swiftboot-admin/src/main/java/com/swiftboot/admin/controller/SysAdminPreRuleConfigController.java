package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.swiftboot.admin.domain.dto.SysAdminPreRuleConfigDTO;
import com.swiftboot.admin.domain.vo.SysAdminPreRuleConfigVO;
import com.swiftboot.admin.service.SysAdminPreRuleConfigService;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrator safety pre-rule configuration controller.
 */
@Tag(name = "Administrator pre-check safety rules")
@RestController
@RequestMapping("/system/ai/admin-pre-rules")
@RequiredArgsConstructor
public class SysAdminPreRuleConfigController {

    private final SysAdminPreRuleConfigService adminPreRuleConfigService;

    @Operation(summary = "Get administrator pre-check safety rules")
    @GetMapping
    @SaCheckPermission("tool:config:list")
    public R<SysAdminPreRuleConfigVO> getConfig() {
        return R.ok(adminPreRuleConfigService.getConfig());
    }

    @Operation(summary = "Update administrator pre-check safety rules")
    @Log(title = "管理员安全前置规则", businessType = BusinessType.UPDATE)
    @PutMapping
    @SaCheckPermission("tool:config:edit")
    public R<Void> updateConfig(@Valid @RequestBody SysAdminPreRuleConfigDTO dto) {
        adminPreRuleConfigService.updateConfig(dto);
        return R.ok();
    }
}
