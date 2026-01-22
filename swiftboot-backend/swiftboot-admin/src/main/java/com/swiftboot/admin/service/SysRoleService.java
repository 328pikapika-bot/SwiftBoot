package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysRole;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;
import java.util.Set;

/**
 * 角色 Service
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    Page<SysRole> selectRolePage(SysRole role, PageQuery pageQuery);

    /**
     * 查询所有角色列表
     */
    List<SysRole> selectRoleAll();

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 根据用户ID查询角色标识集合
     */
    Set<String> selectRoleKeysByUserId(Long userId);

    /**
     * 新增角色
     */
    void insertRole(SysRole role);

    /**
     * 修改角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void deleteRoleByIds(List<Long> roleIds);

    /**
     * 修改状态
     */
    void updateStatus(Long roleId, Integer status);

    /**
     * 校验角色标识是否唯一
     */
    boolean checkRoleKeyUnique(String roleKey, Long roleId);
}
