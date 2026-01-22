package com.swiftboot.admin.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.swiftboot.admin.domain.dto.LoginDTO;
import com.swiftboot.admin.domain.vo.LoginVO;
import com.swiftboot.admin.domain.vo.UserInfoVO;
import com.swiftboot.admin.service.AuthService;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return R.ok(loginVO);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = authService.getUserInfo();
        return R.ok(userInfo);
    }

    /**
     * 生成密码哈希（临时接口，生产环境请删除）
     */
    @Operation(summary = "生成密码哈希")
    @GetMapping("/encrypt/{password}")
    public R<String> encryptPassword(@PathVariable String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return R.ok(hash);
    }
}
