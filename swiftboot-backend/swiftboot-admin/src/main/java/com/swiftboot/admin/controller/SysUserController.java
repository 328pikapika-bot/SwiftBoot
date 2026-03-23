package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.dto.SysUserResetPasswordDTO;
import com.swiftboot.admin.domain.dto.SysUserSaveDTO;
import com.swiftboot.admin.domain.dto.SysUserStatusDTO;
import com.swiftboot.admin.domain.entity.SysUser;
import com.swiftboot.admin.service.SysUserService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.core.result.ResultCode;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return R.ok(userService.selectUserById(userId));
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUserSaveDTO userDTO) {
        userService.insertUser(toUser(userDTO));
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysUserSaveDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
        userService.updateUser(toUser(userDTO));
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
    public R<Void> resetPwd(@Valid @RequestBody SysUserResetPasswordDTO userDTO) {
        userService.resetPassword(userDTO.getId(), userDTO.getPassword());
        return R.ok();
    }

    @Operation(summary = "修改状态")
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Valid @RequestBody SysUserStatusDTO userDTO) {
        userService.updateStatus(userDTO.getId(), userDTO.getStatus());
        return R.ok();
    }

    private SysUser toUser(SysUserSaveDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }
}
