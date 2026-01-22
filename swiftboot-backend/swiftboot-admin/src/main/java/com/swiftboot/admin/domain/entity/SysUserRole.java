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
 * 用户-角色关联（联合主键表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_role")
@Schema(description = "用户-角色关联")
public class SysUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;
}
