package com.swiftboot.test.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.test.service.TestProjectService;
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
 * 示例_项目表 控制器
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-24 00:32:22
 */
@Tag(name = "示例_项目表")
@RestController
@RequestMapping("/test/testProject")
@RequiredArgsConstructor
public class TestProjectController {

    private final TestProjectService testProjectService;

    @Operation(summary = "分页查询示例_项目表列表")
    @SaCheckPermission("test:testProject:list")
    @GetMapping("/list")
    public R<PageResult<TestProject>> list(TestProject testProject, PageQuery pageQuery) {
        Page<TestProject> page = testProjectService.selectTestProjectPage(testProject, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询示例_项目表详情")
    @SaCheckPermission("test:testProject:query")
    @GetMapping("/{id}")
    public R<TestProject> getInfo(@PathVariable Long id) {
        TestProject testProject = testProjectService.getById(id);
        return R.ok(testProject);
    }

    @Operation(summary = "新增示例_项目表")
    @SaCheckPermission("test:testProject:add")
    @Log(title = "示例_项目表", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody TestProject testProject) {
        testProjectService.insertTestProject(testProject);
        return R.ok();
    }

    @Operation(summary = "修改示例_项目表")
    @SaCheckPermission("test:testProject:edit")
    @Log(title = "示例_项目表", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody TestProject testProject) {
        testProjectService.updateTestProject(testProject);
        return R.ok();
    }

    @Operation(summary = "删除示例_项目表")
    @SaCheckPermission("test:testProject:remove")
    @Log(title = "示例_项目表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        testProjectService.deleteTestProjectByIds(ids);
        return R.ok();
    }
}
