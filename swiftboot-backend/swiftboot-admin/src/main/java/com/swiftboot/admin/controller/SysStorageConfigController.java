package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.dto.SysStorageConfigDTO;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.admin.service.SysStorageConfigService;
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
 * Storage configuration controller.
 */
@Tag(name = "Storage configuration")
@RestController
@RequestMapping("/system/storage/config")
@RequiredArgsConstructor
public class SysStorageConfigController {

    private final SysStorageConfigService storageConfigService;

    @Operation(summary = "Get storage config")
    @GetMapping
    public R<SysStorageConfigVO> getConfig() {
        return R.ok(storageConfigService.getConfig());
    }

    @Operation(summary = "Update storage config")
    @Log(title = "Storage configuration", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> updateConfig(@Valid @RequestBody SysStorageConfigDTO dto) {
        storageConfigService.updateConfig(dto);
        return R.ok();
    }
}
