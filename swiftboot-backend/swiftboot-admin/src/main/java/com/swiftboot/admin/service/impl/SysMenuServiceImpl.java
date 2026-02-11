package com.swiftboot.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysMenu;
import com.swiftboot.admin.mapper.SysMenuMapper;
import com.swiftboot.admin.mapper.SysRoleMenuMapper;
import com.swiftboot.admin.service.SysMenuService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(menu.getMenuName() != null, SysMenu::getMenuName, menu.getMenuName());
        wrapper.eq(menu.getStatus() != null, SysMenu::getStatus, menu.getStatus());
        // 查询所有类型（包括按钮），以便角色分配权限
        // wrapper.in(SysMenu::getMenuType, "M", "C");
        wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getSort);
        return list(wrapper);
    }

    @Override
    public List<SysMenu> selectMenuTree(SysMenu menu) {
        List<SysMenu> menus = selectMenuList(menu);
        return buildMenuTree(menus);
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public Set<String> selectPermsByUserId(Long userId) {
        List<String> perms = baseMapper.selectPermsByUserId(userId);
        return new HashSet<>(perms);
    }

    @Override
    public void insertMenu(SysMenu menu) {
        save(menu);
    }

    @Override
    public void updateMenu(SysMenu menu) {
        updateById(menu);
    }

    @Override
    public void deleteMenuById(Long menuId) {
        // 检查是否有子菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, menuId);
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.MENU_HAS_CHILDREN);
        }
        // 删除菜单
        removeById(menuId);
        // 删除角色菜单关联
        roleMenuMapper.deleteByMenuId(menuId);
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        if (CollUtil.isEmpty(menus)) {
            return new ArrayList<>();
        }

        // 构建ID到菜单的映射
        Map<Long, SysMenu> menuMap = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, menu -> menu));

        List<SysMenu> rootMenus = new ArrayList<>();

        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0L) {
                rootMenus.add(menu);
            } else {
                SysMenu parent = menuMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }

        return rootMenus;
    }
}
