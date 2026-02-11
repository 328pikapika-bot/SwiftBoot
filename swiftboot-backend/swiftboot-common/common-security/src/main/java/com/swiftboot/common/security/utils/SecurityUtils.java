package com.swiftboot.common.security.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.swiftboot.common.security.domain.LoginUser;

/**
 * 安全工具类
 */
public class SecurityUtils {

    private static final String LOGIN_USER_KEY = "loginUser";

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取当前登录用户信息
     */
    public static LoginUser getLoginUser() {
        SaSession session = StpUtil.getSession();
        return (LoginUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 设置登录用户信息
     */
    public static void setLoginUser(LoginUser loginUser) {
        SaSession session = StpUtil.getSession();
        session.set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 是否为管理员
     */
    public static boolean isAdmin() {
        return getUserId() != null && getUserId() == 1L;
    }

    /**
     * 是否已登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 检查权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getPermissions() != null && loginUser.getPermissions().contains(permission);
    }

    /**
     * 检查角色
     */
    public static boolean hasRole(String role) {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.getRoles() != null && loginUser.getRoles().contains(role);
    }
}
