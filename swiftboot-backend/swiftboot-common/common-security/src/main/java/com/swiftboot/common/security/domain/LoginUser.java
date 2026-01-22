package com.swiftboot.common.security.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 登录IP
     */
    private String loginIp;
}
