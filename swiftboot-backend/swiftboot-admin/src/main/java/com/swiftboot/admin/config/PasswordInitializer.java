package com.swiftboot.admin.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;

    @Value("${swiftboot.security.bootstrap-default-users-enabled:false}")
    private boolean bootstrapDefaultUsersEnabled;

    @Value("${swiftboot.security.bootstrap-default-password:}")
    private String bootstrapDefaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapDefaultUsersEnabled) {
            log.info("Default user bootstrap is disabled");
            return;
        }
        if (!StringUtils.hasText(bootstrapDefaultPassword)) {
            log.warn("Default user bootstrap is enabled but no bootstrap password is configured");
            return;
        }

        syncUserPassword("admin");
        syncUserPassword("swiftboot");
    }

    private void syncUserPassword(String username) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            log.warn("Skipped bootstrap for missing user: {}", username);
            return;
        }
        if (BCrypt.checkpw(bootstrapDefaultPassword, user.getPassword())) {
            log.info("Bootstrap password already matches user: {}", username);
            return;
        }

        user.setPassword(BCrypt.hashpw(bootstrapDefaultPassword, BCrypt.gensalt()));
        userMapper.updateById(user);
        log.warn("Bootstrap password applied to user: {}", username);
    }
}
