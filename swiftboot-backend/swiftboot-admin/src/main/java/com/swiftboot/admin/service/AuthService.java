package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.dto.LoginDTO;
import com.swiftboot.admin.domain.vo.LoginVO;
import com.swiftboot.admin.domain.vo.UserInfoVO;

/**
 * 认证 Service
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     */
    UserInfoVO getUserInfo();
}
