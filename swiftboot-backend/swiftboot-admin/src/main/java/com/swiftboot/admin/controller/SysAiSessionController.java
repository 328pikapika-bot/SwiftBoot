package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.admin.service.SysAiSessionService;
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
import java.util.Map;

/**
 * 智能会话监控控制器
 */
@Tag(name = "智能会话监控")
@RestController
@RequestMapping("/monitor/ai-session")
@RequiredArgsConstructor
public class SysAiSessionController {

    private final SysAiSessionService aiSessionService;

    @Operation(summary = "分页查询会话记录")
    @SaCheckPermission("monitor:ai-session:list")
    @GetMapping("/list")
    public R<PageResult<SysAiSession>> list(SysAiSession session, PageQuery pageQuery) {
        Page<SysAiSession> page = aiSessionService.selectAiSessionPage(session, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "删除会话记录")
    @SaCheckPermission("monitor:ai-session:remove")
    @Log(title = "智能会话监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        aiSessionService.deleteAiSessionByIds(ids);
        return R.ok();
    }

    @Operation(summary = "清空会话记录")
    @SaCheckPermission("monitor:ai-session:remove")
    @Log(title = "智能会话监控", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        aiSessionService.cleanAiSession();
        return R.ok();
    }

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats(@RequestParam(required = false, defaultValue = "week") String timeRange,
                                      @RequestParam(required = false, defaultValue = "user") String rankType) {
        return R.ok(aiSessionService.getDashboardStats(timeRange, rankType));
    }

    @Operation(summary = "获取用户算力消耗排行（分页）")
    @GetMapping("/user-stats")
    public R<PageResult<Map<String, Object>>> userStats(PageQuery pageQuery, 
                                                      @RequestParam(required = false) String username,
                                                      @RequestParam(required = false, defaultValue = "week") String timeRange,
                                                      @RequestParam(required = false, defaultValue = "user") String rankType) {
        Page<Map<String, Object>> page = aiSessionService.getUserTokenStats(pageQuery, username, timeRange, rankType);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "获取详细交互活跃度统计")
    @GetMapping("/activity-stats")
    public R<Map<String, Object>> activityStats(@RequestParam(required = false, defaultValue = "day") String timeRange) {
        return R.ok(aiSessionService.getDetailedActivityStats(timeRange));
    }
}
