package com.swiftboot.generator.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 代码生成表字段
 */
@Data
@TableName("gen_table_column")
@Schema(description = "代码生成表字段")
public class GenTableColumn implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字段ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "表ID")
    private Long tableId;

    @Schema(description = "字段名称")
    private String columnName;

    @Schema(description = "字段描述")
    private String columnComment;

    @Schema(description = "字段类型")
    private String columnType;

    @Schema(description = "Java类型")
    private String javaType;

    @Schema(description = "Java字段名")
    private String javaField;

    @Schema(description = "是否主键（1是）")
    private String isPk;

    @Schema(description = "是否自增（1是）")
    private String isIncrement;

    @Schema(description = "是否必填（1是）")
    private String isRequired;

    @Schema(description = "是否为插入字段（1是）")
    private String isInsert;

    @Schema(description = "是否为编辑字段（1是）")
    private String isEdit;

    @Schema(description = "是否为列表字段（1是）")
    private String isList;

    @Schema(description = "是否为查询字段（1是）")
    private String isQuery;

    @Schema(description = "查询方式（EQ等于 NE不等于 GT大于 LT小于 LIKE模糊 BETWEEN范围）")
    private String queryType;

    @Schema(description = "显示类型（input文本框 textarea文本域 select下拉框 checkbox复选框 radio单选框 datetime日期控件）")
    private String htmlType;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "排序")
    private Integer sort;
}
