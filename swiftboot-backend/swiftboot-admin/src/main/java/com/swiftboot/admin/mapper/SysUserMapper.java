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

    /**
     * 根据部门ID查询用户ID列表
     */
    @org.apache.ibatis.annotations.Select("select id from sys_user where dept_id = #{deptId} and deleted = 0")
    java.util.List<Long> selectUserIdsByDeptId(@Param("deptId") Long deptId);

    /**
     * 查询部门详情
     */
    @org.apache.ibatis.annotations.Select("select * from sys_dept where id = #{deptId} and status = 0")
    com.swiftboot.admin.domain.entity.SysDept selectDeptById(@Param("deptId") Long deptId);
}
