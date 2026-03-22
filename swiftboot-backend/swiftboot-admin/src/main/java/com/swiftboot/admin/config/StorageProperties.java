package com.swiftboot.admin.config;

import com.swiftboot.admin.domain.dto.SysStorageConfigDTO;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Default storage properties loaded from YAML.
 */
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties extends SysStorageConfigDTO {
}
