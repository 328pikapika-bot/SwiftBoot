package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统菜单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "系统菜单")
public class SysMenu extends BaseEntity {

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型（M目录 C菜单 F按钮）")
    private String menuType;

    @Schema(description = "路由地址")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否可见（0显示 1隐藏）")
    private Integer visible;

    @Schema(description = "状态（0正常 1禁用）")
    private Integer status;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    @Schema(description = "子菜单")
    private List<SysMenu> children;
}
