package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.service.SysUserService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "分页查询用户列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(SysUser user, PageQuery pageQuery) {
        Page<SysUser> page = userService.selectUserPage(user, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询用户详情")
    @SaCheckPermission("system:user:query")
    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        SysUser user = userService.selectUserById(userId);
        return R.ok(user);
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUser user) {
        userService.insertUser(user);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysUser user) {
        userService.updateUser(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable List<Long> userIds) {
        userService.deleteUserByIds(userIds);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUser user) {
        userService.resetPassword(user.getId(), user.getPassword());
        return R.ok();
    }

    @Operation(summary = "修改状态")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUser user) {
        userService.updateStatus(user.getId(), user.getStatus());
        return R.ok();
    }
}
