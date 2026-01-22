package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-菜单关联 Mapper
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 删除角色的菜单关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除菜单的角色关联
     */
    int deleteByMenuId(@Param("menuId") Long menuId);
}
