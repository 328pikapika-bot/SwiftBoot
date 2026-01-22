package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.swiftboot.admin.domain.entity.SysMenu;
import com.swiftboot.admin.service.SysMenuService;
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
 * 菜单控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "查询菜单列表")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public R<List<SysMenu>> list(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuList(menu);
        return R.ok(menus);
    }

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree(SysMenu menu) {
        List<SysMenu> menus = menuService.selectMenuTree(menu);
        return R.ok(menus);
    }

    @Operation(summary = "根据角色ID查询菜单ID列表")
    @GetMapping("/roleMenuIds/{roleId}")
    public R<List<Long>> roleMenuIds(@PathVariable Long roleId) {
        List<Long> menuIds = menuService.selectMenuIdsByRoleId(roleId);
        return R.ok(menuIds);
    }

    @Operation(summary = "查询菜单详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        SysMenu menu = menuService.getById(menuId);
        return R.ok(menu);
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysMenu menu) {
        menuService.insertMenu(menu);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        menuService.deleteMenuById(menuId);
        return R.ok();
    }
}
