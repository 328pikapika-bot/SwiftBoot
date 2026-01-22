package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户列表
     */
    Page<SysUser> selectUserPage(Page<SysUser> page, @Param("user") SysUser user);

    /**
     * 查询用户详情（含角色）
     */
    SysUser selectUserById(@Param("userId") Long userId);
}
