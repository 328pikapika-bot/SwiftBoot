package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 用户 Service
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(String username);

    /**
     * 分页查询用户列表
     */
    Page<SysUser> selectUserPage(SysUser user, PageQuery pageQuery);

    /**
     * 查询用户详情
     */
    SysUser selectUserById(Long userId);

    /**
     * 新增用户
     */
    void insertUser(SysUser user);

    /**
     * 修改用户
     */
    void updateUser(SysUser user);

    /**
     * 删除用户
     */
    void deleteUserByIds(List<Long> userIds);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String password);

    /**
     * 修改状态
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 校验用户名是否唯一
     */
    boolean checkUsernameUnique(String username, Long userId);
}
