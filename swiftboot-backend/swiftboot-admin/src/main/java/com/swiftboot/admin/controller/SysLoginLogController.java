package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysLoginLog;
import com.swiftboot.admin.service.SysLoginLogService;
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

/**
 * 登录日志控制器
 */
@Tag(name = "登录日志")
@RestController
@RequestMapping("/monitor/loginlog")
@RequiredArgsConstructor
public class SysLoginLogController {

    private final SysLoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志列表")
    @SaCheckPermission("monitor:loginlog:list")
    @GetMapping("/list")
    public R<PageResult<SysLoginLog>> list(SysLoginLog loginLog, PageQuery pageQuery) {
        Page<SysLoginLog> page = loginLogService.selectLoginLogPage(loginLog, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "删除登录日志")
    @SaCheckPermission("monitor:loginlog:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{loginLogIds}")
    public R<Void> remove(@PathVariable List<Long> loginLogIds) {
        loginLogService.deleteLoginLogByIds(loginLogIds);
        return R.ok();
    }

    @Operation(summary = "清空登录日志")
    @SaCheckPermission("monitor:loginlog:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginLogService.cleanLoginLog();
        return R.ok();
    }
}
