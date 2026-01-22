package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统部门
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
@Schema(description = "系统部门")
public class SysDept extends BaseEntity {

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "默认角色ID（新增用户时的默认角色）")
    private Long defaultRoleId;

    @Schema(description = "状态（0正常 1禁用）")
    private Integer status;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    @Schema(description = "默认角色名称")
    private String defaultRoleName;

    @TableField(exist = false)
    @Schema(description = "子部门")
    private List<SysDept> children;
}
