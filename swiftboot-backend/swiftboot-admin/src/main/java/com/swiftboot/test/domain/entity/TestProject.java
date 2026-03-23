package com.swiftboot.test.domain.entity;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目示例实体
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-24 00:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_project")
@Schema(description = "项目示例")
public class TestProject extends BaseEntity {

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目编号")
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
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "项目预算")
    private BigDecimal budget;

    @Schema(description = "项目进度（0-100）")
    private Integer progress;

    @Schema(description = "状态（0进行中 1已完成 2已暂停 3已取消）")
    private Integer status;

    @Schema(description = "优先级（1低 2中 3高）")
    private Integer priority;

    @Schema(description = "项目描述")
    private String description;

}
