package com.swiftboot.common.security.utils;

import com.swiftboot.common.security.domain.LoginUser;

import java.util.HashSet;
import java.util.Set;

/**
 * Built-in permission grants for special roles.
 */
public final class PermissionGrantUtils {

    public static final String ADMIN_ROLE_KEY = "admin";
    public static final String CONFIG_LIST_PERMISSION = "tool:config:list";
    public static final String CONFIG_EDIT_PERMISSION = "tool:config:edit";

    private PermissionGrantUtils() {
    }

    public static Set<String> mergeBuiltinPermissions(LoginUser loginUser) {
        Set<String> permissions = new HashSet<>();
        if (loginUser == null) {
            return permissions;
        }
        if (loginUser.getPermissions() != null) {
            permissions.addAll(loginUser.getPermissions());
        }
        if (loginUser.getRoles() != null && loginUser.getRoles().contains(ADMIN_ROLE_KEY)) {
            permissions.add(CONFIG_LIST_PERMISSION);
            permissions.add(CONFIG_EDIT_PERMISSION);
        }
        return permissions;
    }

    public static Set<String> mergeBuiltinPermissions(Set<String> roles, Set<String> permissions) {
        LoginUser loginUser = new LoginUser();
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        return mergeBuiltinPermissions(loginUser);
    }
}
