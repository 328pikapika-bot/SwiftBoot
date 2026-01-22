package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色标识查询角色
     */
    SysRole selectByRoleKey(@Param("roleKey") String roleKey);
}
