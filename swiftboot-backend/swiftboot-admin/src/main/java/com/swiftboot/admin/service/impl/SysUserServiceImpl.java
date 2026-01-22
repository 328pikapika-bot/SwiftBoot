package com.swiftboot.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.domain.entity.SysUserRole;
import com.swiftboot.admin.mapper.SysUserMapper;
import com.swiftboot.admin.mapper.SysUserRoleMapper;
import com.swiftboot.admin.service.SysUserService;
import com.swiftboot.common.core.constant.Constants;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public SysUser selectByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public Page<SysUser> selectUserPage(SysUser user, PageQuery pageQuery) {
        Page<SysUser> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return baseMapper.selectUserPage(page, user);
    }

    @Override
    public SysUser selectUserById(Long userId) {
        SysUser user = baseMapper.selectUserById(userId);
        if (user != null && CollUtil.isNotEmpty(user.getRoles())) {
            user.setRoleIds(user.getRoles().stream().map(r -> r.getId()).toList());
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUser(SysUser user) {
        // 校验用户名唯一
        if (!checkUsernameUnique(user.getUsername(), null)) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // 加密密码
        String password = StrUtil.isNotBlank(user.getPassword()) ? user.getPassword() : Constants.DEFAULT_PASSWORD;
        user.setPassword(BCrypt.hashpw(password));
        // 保存用户
        save(user);
        // 保存用户角色关联
        insertUserRole(user.getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        // 校验用户名唯一
        if (!checkUsernameUnique(user.getUsername(), user.getId())) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // 不允许修改密码
        user.setPassword(null);
        // 更新用户
        updateById(user);
        // 删除原有角色关联
        userRoleMapper.deleteByUserId(user.getId());
        // 保存新的角色关联
        insertUserRole(user.getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByIds(List<Long> userIds) {
        // 不允许删除管理员
        if (userIds.contains(Constants.SUPER_ADMIN_ID)) {
            throw new BusinessException("不允许删除超级管理员");
        }
        // 删除用户
        removeByIds(userIds);
        // 删除用户角色关联
        userIds.forEach(userRoleMapper::deleteByUserId);
    }

    @Override
    public void resetPassword(Long userId, String password) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(password));
        updateById(user);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        // 不允许禁用管理员
        if (Constants.SUPER_ADMIN_ID.equals(userId) && Constants.STATUS_DISABLE.equals(status)) {
            throw new BusinessException("不允许禁用超级管理员");
        }
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public boolean checkUsernameUnique(String username, Long userId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userId != null) {
            wrapper.ne(SysUser::getId, userId);
        }
        return count(wrapper) == 0;
    }

    /**
     * 保存用户角色关联
     */
    private void insertUserRole(Long userId, List<Long> roleIds) {
        if (CollUtil.isNotEmpty(roleIds)) {
            List<SysUserRole> userRoles = roleIds.stream()
                    .map(roleId -> new SysUserRole(userId, roleId))
                    .toList();
            userRoles.forEach(userRoleMapper::insert);
        }
    }
}
