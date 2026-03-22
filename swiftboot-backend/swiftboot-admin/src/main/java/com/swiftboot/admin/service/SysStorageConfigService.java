package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.dto.SysStorageConfigDTO;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;

/**
 * Storage config service.
 */
public interface SysStorageConfigService {

    SysStorageConfigVO getConfig();

    SysStorageConfigVO getRuntimeConfig();

    void updateConfig(SysStorageConfigDTO dto);
}
