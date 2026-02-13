package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.swiftboot.admin.event.OperLogEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService operLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Operation(summary = "分页查询操作日志列表")
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/list")
    public R<PageResult<SysOperLog>> list(SysOperLog operLog, PageQuery pageQuery, 
                                        @RequestParam(required = false) String logType) {
        Page<SysOperLog> page = operLogService.selectOperLogPage(operLog, pageQuery, logType);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operLogIds}")
    public R<Void> remove(@PathVariable List<Long> operLogIds) {
        operLogService.deleteOperLogByIds(operLogIds);
        return R.ok();
    }


    @Operation(summary = "清空操作日志")
    @SaCheckPermission("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }

    @Operation(summary = "获取操作模块列表")
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/module/list")
    public R<List<String>> moduleList() {
        return R.ok(operLogService.selectOperLogModuleList());
    }

    @Operation(summary = "内部接口：添加操作日志")
    @PostMapping("/inner/add")
    public R<Void> innerAdd(@RequestBody SysOperLog operLog) {
        operLogService.save(operLog);
        eventPublisher.publishEvent(new OperLogEvent(this, operLog));
        return R.ok();
    }
}
