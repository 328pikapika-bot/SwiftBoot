package com.swiftboot.generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 代码生成表
 */
@Data
@TableName("gen_table")
@Schema(description = "代码生成表")
public class GenTable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "表ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "表名称")
    private String tableName;

    @Schema(description = "表描述")
    private String tableComment;

    @Schema(description = "实体类名称")
    private String className;

    @Schema(description = "生成包路径")
    private String packageName;

    @Schema(description = "生成模块名")
    private String moduleName;

    @Schema(description = "生成业务名")
    private String businessName;

    @Schema(description = "生成功能名")
    private String functionName;

    @Schema(description = "生成作者")
    private String author;

    @Schema(description = "生成路径（不填默认项目路径）")
    private String genPath;

    @Schema(description = "生成代码方式（0zip压缩包 1自定义路径）")
    private String genType;

    @Schema(description = "其他生成选项")
    private String options;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    @TableField(exist = false)
    @Schema(description = "表列信息")
    private List<GenTableColumn> columns;

    @TableField(exist = false)
    @Schema(description = "主键列信息")
    private GenTableColumn pkColumn;
}
