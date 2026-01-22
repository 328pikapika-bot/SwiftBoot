package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_file")
@Schema(description = "文件信息")
public class SysFile extends BaseEntity {

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "原始名称")
    private String originalName;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "存储类型")
    private String storageType;

    @Schema(description = "访问地址")
    private String url;
}
