package com.swiftboot.admin.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysRole;
import com.swiftboot.admin.domain.entity.SysRoleMenu;
import com.swiftboot.admin.mapper.SysMenuMapper;
import com.swiftboot.admin.mapper.SysRoleMapper;
import com.swiftboot.admin.mapper.SysRoleMenuMapper;
import com.swiftboot.admin.mapper.SysUserRoleMapper;
import com.swiftboot.admin.service.SysRoleService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import com.swiftboot.common.security.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色 Service 实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public Page<SysRole> selectRolePage(SysRole role, PageQuery pageQuery) {
        Page<SysRole> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(role.getRoleName() != null, SysRole::getRoleName, role.getRoleName());
        wrapper.like(role.getRoleKey() != null, SysRole::getRoleKey, role.getRoleKey());
        wrapper.eq(role.getStatus() != null, SysRole::getStatus, role.getStatus());
        wrapper.orderByAsc(SysRole::getSort);
        return page(page, wrapper);
    }

    @Override
    public List<SysRole> selectRoleAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 0);
        wrapper.orderByAsc(SysRole::getSort);
        return list(wrapper);
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    public Set<String> selectRoleKeysByUserId(Long userId) {
        List<SysRole> roles = selectRolesByUserId(userId);
        return roles.stream()
                .map(SysRole::getRoleKey)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertRole(SysRole role) {
        // 校验角色标识唯一
        if (!checkRoleKeyUnique(role.getRoleKey(), null)) {
            throw new BusinessException(ResultCode.ROLE_EXISTS);
        }
        // 保存角色
        save(role);
        // 保存角色菜单关联
        insertRoleMenu(role.getId(), role.getMenuIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        // 校验角色标识唯一
        if (!checkRoleKeyUnique(role.getRoleKey(), role.getId())) {
            throw new BusinessException(ResultCode.ROLE_EXISTS);
        }
        // 更新角色
        updateById(role);
        // 删除原有菜单关联
        roleMenuMapper.deleteByRoleId(role.getId());
        // 保存新的菜单关联
        insertRoleMenu(role.getId(), role.getMenuIds());

        // 刷新所有在线用户的权限缓存 (静默更新方式)
        // 方案：找到所有拥有该角色的用户，更新其 Session 中的权限缓存，无需重新登录
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(role.getId());
        if (CollUtil.isNotEmpty(userIds)) {
            for (Long userId : userIds) {
                // 仅处理在线用户
                if (StpUtil.isLogin(userId)) {
                    try {
                        // 获取用户 Session，如果不在线则不创建
                        SaSession session = StpUtil.getSessionByLoginId(userId, false);
                        if (session != null) {
                            // 获取 Session 中的 LoginUser
                            LoginUser loginUser = (LoginUser) session.get("loginUser");
                            if (loginUser != null) {
                                // 重新查询权限
                                Set<String> permissions = new HashSet<>(menuMapper.selectPermsByUserId(userId));
                                // 更新权限
                                loginUser.setPermissions(permissions);
                                // 重新写入 Session
                                session.set("loginUser", loginUser);
                                log.debug("Refreshed permissions for user: {}", userId);
                            }
                        }
                    } catch (Exception e) {
                        // 忽略异常，防止影响主流程
                        log.warn("Failed to refresh permissions for user: {}", userId, e);
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleByIds(List<Long> roleIds) {
        for (Long roleId : roleIds) {
            // 检查是否有用户关联
            LambdaQueryWrapper<com.swiftboot.admin.domain.entity.SysUserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(com.swiftboot.admin.domain.entity.SysUserRole::getRoleId, roleId);
            if (userRoleMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ResultCode.ROLE_IN_USE);
            }
        }
        // 删除角色
        removeByIds(roleIds);
        // 删除角色菜单关联
        roleIds.forEach(roleMenuMapper::deleteByRoleId);
    }

    @Override
    public void updateStatus(Long roleId, Integer status) {
        SysRole role = new SysRole();
        role.setId(roleId);
        role.setStatus(status);
        updateById(role);
    }

    @Override
    public boolean checkRoleKeyUnique(String roleKey, Long roleId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        if (roleId != null) {
            wrapper.ne(SysRole::getId, roleId);
        }
        return count(wrapper) == 0;
    }

    /**
     * 保存角色菜单关联
     */
    private void insertRoleMenu(Long roleId, List<Long> menuIds) {
        if (CollUtil.isNotEmpty(menuIds)) {
            List<SysRoleMenu> roleMenus = menuIds.stream()
                    .map(menuId -> new SysRoleMenu(roleId, menuId))
                    .toList();
            roleMenus.forEach(roleMenuMapper::insert);
        }
    }
}
