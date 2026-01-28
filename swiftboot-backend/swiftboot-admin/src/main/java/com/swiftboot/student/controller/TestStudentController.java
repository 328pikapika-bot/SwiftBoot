package com.swiftboot.student.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.student.domain.entity.TestStudent;
import com.swiftboot.student.service.TestStudentService;
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
 * 测试学生表 控制器
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-28 21:47:45
 */
@Tag(name = "测试学生表")
@RestController
@RequestMapping("/student/testStudent")
@RequiredArgsConstructor
public class TestStudentController {

    private final TestStudentService testStudentService;

    @Operation(summary = "分页查询测试学生表列表")
    @SaCheckPermission("student:testStudent:list")
    @GetMapping("/list")
    public R<PageResult<TestStudent>> list(TestStudent testStudent, PageQuery pageQuery) {
        Page<TestStudent> page = testStudentService.selectTestStudentPage(testStudent, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询测试学生表详情")
    @SaCheckPermission("student:testStudent:query")
    @GetMapping("/{id}")
    public R<TestStudent> getInfo(@PathVariable Long id) {
        TestStudent testStudent = testStudentService.getById(id);
        return R.ok(testStudent);
    }

    @Operation(summary = "新增测试学生表")
    @SaCheckPermission("student:testStudent:add")
    @Log(title = "测试学生表", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Valid @RequestBody TestStudent testStudent) {
        testStudentService.insertTestStudent(testStudent);
        return R.ok();
    }

    @Operation(summary = "修改测试学生表")
    @SaCheckPermission("student:testStudent:edit")
    @Log(title = "测试学生表", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Valid @RequestBody TestStudent testStudent) {
        testStudentService.updateTestStudent(testStudent);
        return R.ok();
    }

    @Operation(summary = "删除测试学生表")
    @SaCheckPermission("student:testStudent:remove")
    @Log(title = "测试学生表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        testStudentService.deleteTestStudentByIds(ids);
        return R.ok();
    }
}
