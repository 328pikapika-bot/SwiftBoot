package com.swiftboot.admin.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 密码初始化器 - 启动时检查并修复默认用户密码
 * 生产环境请删除此类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 默认密码
        String defaultPassword = "123456";
        String correctHash = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());
        
        // 检查 admin 用户
        SysUser admin = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );
        
        if (admin != null) {
            // 检查当前密码是否正确
            if (!BCrypt.checkpw(defaultPassword, admin.getPassword())) {
                // 密码不正确，更新为正确的哈希
                admin.setPassword(correctHash);
                userMapper.updateById(admin);
                log.info("✅ 已自动修复 admin 用户密码");
            }
        }
        
        // 检查 swiftboot 用户
        SysUser swiftboot = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "swiftboot")
        );
        
        if (swiftboot != null) {
            if (!BCrypt.checkpw(defaultPassword, swiftboot.getPassword())) {
                swiftboot.setPassword(correctHash);
                userMapper.updateById(swiftboot);
                log.info("✅ 已自动修复 swiftboot 用户密码");
            }
        }
        
        log.info("🚀 密码初始化检查完成，默认账号: admin/123456, swiftboot/123456");
    }
}
