package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysRole;
import com.swiftboot.admin.service.SysRoleService;
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
 * 角色控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<PageResult<SysRole>> list(SysRole role, PageQuery pageQuery) {
        Page<SysRole> page = roleService.selectRolePage(role, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询所有角色")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        List<SysRole> roles = roleService.selectRoleAll();
        return R.ok(roles);
    }

    @Operation(summary = "查询角色详情")
    @SaCheckPermission("system:role:query")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole role = roleService.getById(roleId);
        return R.ok(role);
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysRole role) {
        roleService.insertRole(role);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysRole role) {
        roleService.updateRole(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable List<Long> roleIds) {
        roleService.deleteRoleByIds(roleIds);
        return R.ok();
    }

    @Operation(summary = "修改状态")
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRole role) {
        roleService.updateStatus(role.getId(), role.getStatus());
        return R.ok();
    }
}
