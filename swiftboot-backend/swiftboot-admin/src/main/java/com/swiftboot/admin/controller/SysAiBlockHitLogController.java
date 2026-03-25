package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysAiBlockHitLog;
import com.swiftboot.admin.service.SysAiBlockHitLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI block word hit log controller.
 */
@Tag(name = "AI屏蔽词命中日志")
@RestController
@RequestMapping("/monitor/ai-block-hit")
@RequiredArgsConstructor
public class SysAiBlockHitLogController {

    private final SysAiBlockHitLogService blockHitLogService;

    @Operation(summary = "分页查询屏蔽词命中日志")
    @GetMapping("/list")
    @SaCheckPermission("monitor:ai-block-hit:list")
    public R<PageResult<SysAiBlockHitLog>> list(SysAiBlockHitLog query, PageQuery pageQuery) {
        Page<SysAiBlockHitLog> page = blockHitLogService.selectPage(query, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "获取屏蔽词命中统计")
    @GetMapping("/stats")
    @SaCheckPermission("monitor:ai-block-hit:list")
    public R<Map<String, Object>> stats() {
        return R.ok(blockHitLogService.getStats());
    }
}
