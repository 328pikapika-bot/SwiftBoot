package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统角色
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "系统角色")
public class SysRole extends BaseEntity {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识")
    private String roleKey;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态（0正常 1禁用）")
    private Integer status;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
