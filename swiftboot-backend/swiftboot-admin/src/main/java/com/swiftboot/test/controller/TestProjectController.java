package com.swiftboot.test.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.test.domain.vo.TestProjectImportResultVO;
import com.swiftboot.test.service.TestProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 示例项目控制器
 */
@Tag(name = "示例项目")
@RestController
@RequestMapping("/test/testProject")
@RequiredArgsConstructor
public class TestProjectController {

    private final TestProjectService testProjectService;

    @Operation(summary = "分页查询示例项目列表")
    @SaCheckPermission("test:testProject:list")
    @GetMapping("/list")
    public R<PageResult<TestProject>> list(TestProject testProject, PageQuery pageQuery) {
        Page<TestProject> page = testProjectService.selectTestProjectPage(testProject, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询示例项目详情")
    @SaCheckPermission("test:testProject:query")
    @GetMapping("/{id}")
    public R<TestProject> getInfo(@PathVariable Long id) {
        return R.ok(testProjectService.getById(id));
    }

    @Operation(summary = "新增示例项目")
    @SaCheckPermission("test:testProject:add")
    @Log(title = "示例项目", businessType = BusinessType.INSERT)
    @PostMapping
    public R<TestProject> add(@Valid @RequestBody TestProject testProject) {
        testProjectService.insertTestProject(testProject);
        return R.ok(testProject);
    }

    @Operation(summary = "修改示例项目")
    @SaCheckPermission("test:testProject:edit")
    @Log(title = "示例项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<TestProject> edit(@Valid @RequestBody TestProject testProject) {
        testProjectService.updateTestProject(testProject);
        return R.ok(testProject);
    }

    @Operation(summary = "删除示例项目")
    @SaCheckPermission("test:testProject:remove")
    @Log(title = "示例项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        testProjectService.deleteTestProjectByIds(ids);
        return R.ok();
    }

    @Operation(summary = "导出示例项目")
    @SaCheckPermission("test:testProject:export")
    @Log(title = "示例项目", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(TestProject testProject,
                       @RequestParam(required = false) String ids,
                       HttpServletResponse response) {
        testProjectService.exportTestProject(testProject, parseIds(ids), response);
    }

    @Operation(summary = "导入示例项目")
    @SaCheckPermission("test:testProject:import")
    @Log(title = "示例项目", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public R<TestProjectImportResultVO> importData(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(defaultValue = "false") boolean updateSupport) {
        return R.ok(testProjectService.importTestProject(file, updateSupport));
    }

    @Operation(summary = "下载导入模板")
    @SaCheckPermission("test:testProject:template")
    @GetMapping("/import-template")
    public void importTemplate(HttpServletResponse response) {
        testProjectService.downloadImportTemplate(response);
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
