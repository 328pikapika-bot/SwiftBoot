package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色-菜单关联（联合主键表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_menu")
@Schema(description = "角色-菜单关联")
public class SysRoleMenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    @TableField("menu_id")
    @Schema(description = "菜单ID")
    private Long menuId;
}
