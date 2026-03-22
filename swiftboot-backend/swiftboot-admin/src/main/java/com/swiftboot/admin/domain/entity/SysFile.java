package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * File metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
@Schema(description = "File metadata")
public class SysFile extends BaseEntity {

    @Schema(description = "Stored file name")
    private String fileName;

    @Schema(description = "Original file name")
    private String originalName;

    @Schema(description = "File suffix")
    private String fileSuffix;

    @Schema(description = "Object key or relative path")
    private String filePath;

    @Schema(description = "File size")
    private Long fileSize;

    @Schema(description = "Storage type")
    private String storageType;

    @Schema(description = "Storage bucket")
    private String storageBucket;

    @Schema(description = "MIME type")
    private String mimeType;

    @Schema(description = "Visibility")
    private String visibility;

    @Schema(description = "Business type")
    private String bizType;

    @Schema(description = "Business ID")
    private Long bizId;

    @Schema(description = "Public URL when available")
    private String url;
}
