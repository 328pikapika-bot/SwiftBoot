package com.swiftboot.test.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 示例项目导入导出行模型
 */
@Data
@Schema(description = "示例项目导入导出行")
public class TestProjectExcelVO {

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("项目编号")
    private String projectCode;

    @ExcelProperty("项目类型")
    private String projectType;

    @ExcelProperty("项目经理")
    private String managerName;

    @ExcelProperty("开始日期")
    private String startDate;

    @ExcelProperty("结束日期")
    private String endDate;

    @ExcelProperty("项目预算")
    private String budget;

    @ExcelProperty("项目进度")
    private String progress;

    @ExcelProperty("项目状态")
    private String status;

    @ExcelProperty("优先级")
    private String priority;

    @ExcelProperty("项目描述")
    private String description;

    @ExcelProperty("备注")
    private String remark;
}
