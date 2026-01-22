package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.swiftboot.admin.domain.entity.SysDept;
import com.swiftboot.admin.service.SysDeptService;
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
 * 部门控制器
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @Operation(summary = "查询部门列表")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public R<List<SysDept>> list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    public R<List<SysDept>> tree(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptTree(dept);
        return R.ok(depts);
    }

    @Operation(summary = "查询部门详情")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{deptId}")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        SysDept dept = deptService.getById(deptId);
        return R.ok(dept);
    }

    @Operation(summary = "新增部门")
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDept dept) {
        deptService.insertDept(dept);
        return R.ok();
    }

    @Operation(summary = "修改部门")
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @SaCheckPermission("system:dept:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        deptService.deleteDeptById(deptId);
        return R.ok();
    }
}
