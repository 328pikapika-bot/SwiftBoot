package com.swiftboot.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import com.swiftboot.generator.domain.GenTable;
import com.swiftboot.generator.service.GenTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 */
@Tag(name = "代码生成")
@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
public class GenController {

    private final GenTableService genTableService;

    @Operation(summary = "分页查询代码生成列表")
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/list")
    public R<PageResult<GenTable>> list(GenTable genTable, PageQuery pageQuery) {
        Page<GenTable> page = genTableService.selectGenTablePage(genTable, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "查询数据库表列表")
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/db/list")
    public R<List<GenTable>> dbList(String tableName, String tableComment) {
        List<GenTable> list = genTableService.selectDbTableList(tableName, tableComment);
        return R.ok(list);
    }

    @Operation(summary = "查询表详情")
    @SaCheckPermission("tool:gen:query")
    @GetMapping("/{tableId}")
    public R<GenTable> getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        return R.ok(table);
    }

    @Operation(summary = "导入表")
    @SaCheckPermission("tool:gen:import")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public R<Void> importTable(@RequestBody String[] tableNames) {
        genTableService.importTable(tableNames);
        return R.ok();
    }

    @Operation(summary = "修改代码生成信息")
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody GenTable genTable) {
        genTableService.updateGenTable(genTable);
        return R.ok();
    }

    @Operation(summary = "删除代码生成")
    @SaCheckPermission("tool:gen:remove")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public R<Void> remove(@PathVariable List<Long> tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return R.ok();
    }

    @Operation(summary = "预览代码")
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    public R<Map<String, String>> preview(@PathVariable Long tableId) {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return R.ok(dataMap);
    }

    @Operation(summary = "下载代码")
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENERATE)
    @GetMapping("/download/{tableName}")
    public void download(@PathVariable String tableName, HttpServletResponse response) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    @Operation(summary = "生成代码（自定义路径）")
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENERATE)
    @GetMapping("/generate/{tableName}")
    public R<Void> generate(@PathVariable String tableName) {
        genTableService.generateCode(tableName);
        return R.ok();
    }

    @Operation(summary = "同步数据库")
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @GetMapping("/syncDb/{tableName}")
    public R<Void> syncDb(@PathVariable String tableName) {
        genTableService.syncDb(tableName);
        return R.ok();
    }

    @Operation(summary = "批量下载代码")
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENERATE)
    @GetMapping("/batchDownload")
    public void batchDownload(String[] tableNames, HttpServletResponse response) throws IOException {
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=swiftboot-code.zip");
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}
