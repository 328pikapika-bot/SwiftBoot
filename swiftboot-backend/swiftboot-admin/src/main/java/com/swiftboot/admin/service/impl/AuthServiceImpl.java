package com.swiftboot.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.swiftboot.admin.domain.dto.LoginDTO;
import com.swiftboot.admin.domain.entity.SysLoginLog;
import com.swiftboot.admin.domain.entity.SysMenu;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.domain.vo.LoginVO;
import com.swiftboot.admin.domain.vo.UserInfoVO;
import com.swiftboot.admin.service.*;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import com.swiftboot.common.security.domain.LoginUser;
import com.swiftboot.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 认证 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final SysLoginLogService loginLogService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 查询用户
        SysUser user = userService.selectByUsername(username);

        // 记录登录日志
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setLoginIp(getClientIp());

        try {
            // 用户不存在
            if (user == null) {
                loginLog.setStatus(1);
                loginLog.setMsg("用户不存在");
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
            }

            // 密码错误
            if (!BCrypt.checkpw(password, user.getPassword())) {
                loginLog.setStatus(1);
                loginLog.setMsg("密码错误");
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
            }

            // 用户被禁用
            if (user.getStatus() != 0) {
                loginLog.setStatus(1);
                loginLog.setMsg("用户已被禁用");
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            // 登录成功
            StpUtil.login(user.getId());

            // 查询角色和权限
            Set<String> roles = roleService.selectRoleKeysByUserId(user.getId());
            Set<String> permissions = menuService.selectPermsByUserId(user.getId());

            // 构建登录用户信息
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            loginUser.setNickname(user.getNickname());
            loginUser.setAvatar(user.getAvatar());
            loginUser.setDeptId(user.getDeptId());
            loginUser.setDeptName(user.getDeptName());
            loginUser.setRoles(roles);
            loginUser.setPermissions(permissions);
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setLoginIp(getClientIp());

            // 保存到 Session
            SecurityUtils.setLoginUser(loginUser);

            // 更新用户登录信息
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setLoginIp(getClientIp());
            updateUser.setLoginDate(LocalDateTime.now());
            userService.updateById(updateUser);

            // 记录登录成功日志
            loginLog.setStatus(0);
            loginLog.setMsg("登录成功");

            return new LoginVO(StpUtil.getTokenValue());

        } finally {
            // 异步保存登录日志
            loginLogService.saveLoginLog(loginLog);
        }
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getUserInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(loginUser.getUserId());
        userInfo.setUsername(loginUser.getUsername());
        userInfo.setNickname(loginUser.getNickname());
        userInfo.setAvatar(loginUser.getAvatar());
        userInfo.setRoles(loginUser.getRoles());
        userInfo.setPermissions(loginUser.getPermissions());

        // 查询菜单
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(loginUser.getUserId());
        userInfo.setMenus(menus);

        return userInfo;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return JakartaServletUtil.getClientIP(request);
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}
