package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户-角色关联 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 删除用户的角色关联
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的用户关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
}
