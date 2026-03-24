package com.swiftboot.common.security.handler;

import cn.dev33.satoken.stp.StpInterface;
import com.swiftboot.common.security.domain.LoginUser;
import com.swiftboot.common.security.utils.PermissionGrantUtils;
import com.swiftboot.common.security.utils.SecurityUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(PermissionGrantUtils.mergeBuiltinPermissions(loginUser));
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getRoles() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(loginUser.getRoles());
    }
}
