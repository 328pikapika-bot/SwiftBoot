package com.swiftboot.admin.domain.vo;

import com.swiftboot.admin.domain.dto.SysStorageConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;

/**
 * Storage configuration view object.
 */
@Schema(description = "Storage configuration")
@EqualsAndHashCode(callSuper = true)
public class SysStorageConfigVO extends SysStorageConfigDTO {
}
