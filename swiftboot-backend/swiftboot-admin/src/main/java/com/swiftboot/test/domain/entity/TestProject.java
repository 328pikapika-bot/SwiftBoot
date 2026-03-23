package com.swiftboot.test.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 示例项目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_project")
@Schema(description = "示例项目")
public class TestProject extends BaseEntity {

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    private String projectName;

    @Schema(description = "项目编号")
    @NotBlank(message = "项目编号不能为空")
    @Size(max = 50, message = "项目编号长度不能超过50个字符")
    private String projectCode;

    @Schema(description = "项目类型（1内部项目 2外包项目 3合作项目）")
    private Integer projectType;

    @Schema(description = "项目经理ID")
    private Long managerId;

    @Schema(description = "项目经理姓名")
    private String managerName;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "项目预算")
    private BigDecimal budget;

    @Schema(description = "项目进度（0-100）")
    @Min(value = 0, message = "项目进度不能小于0")
    @Max(value = 100, message = "项目进度不能大于100")
    private Integer progress;

    @Schema(description = "状态（0进行中 1已完成 2已暂停 3已取消）")
    private Integer status;

    @Schema(description = "优先级（1低 2中 3高）")
    private Integer priority;

    @Schema(description = "项目描述")
    private String description;
}
