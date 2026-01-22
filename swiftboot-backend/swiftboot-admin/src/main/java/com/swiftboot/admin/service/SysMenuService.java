package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 菜单 Service
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单列表
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 查询菜单树
     */
    List<SysMenu> selectMenuTree(SysMenu menu);

    /**
     * 根据用户ID查询菜单树
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限列表
     */
    Set<String> selectPermsByUserId(Long userId);

    /**
     * 新增菜单
     */
    void insertMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    void updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    void deleteMenuById(Long menuId);

    /**
     * 构建菜单树
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);
}
