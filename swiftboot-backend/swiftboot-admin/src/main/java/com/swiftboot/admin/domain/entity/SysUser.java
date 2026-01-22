package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends BaseEntity {

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别（0男 1女 2未知）")
    private Integer gender;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "状态（0正常 1禁用）")
    private Integer status;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;

    @TableField(exist = false)
    @Schema(description = "角色列表")
    private List<SysRole> roles;

    @TableField(exist = false)
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
