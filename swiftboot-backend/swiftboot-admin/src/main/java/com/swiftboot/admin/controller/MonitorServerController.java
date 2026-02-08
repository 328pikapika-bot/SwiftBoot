package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swiftboot.admin.domain.monitor.SysMonitorLog;
import com.swiftboot.admin.domain.server.Server;
import com.swiftboot.admin.mapper.SysMonitorLogMapper;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器监控
 */
@Tag(name = "服务监控")
@RestController
@RequestMapping("/monitor/server")
@RequiredArgsConstructor
public class MonitorServerController {

    private final SysMonitorLogMapper sysMonitorLogMapper;

    @Operation(summary = "获取服务器监控信息")
    @SaCheckPermission("monitor:server:list")
    @GetMapping
    public R<Server> getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return R.ok(server);
    }

    @Operation(summary = "获取历史监控数据")
    @SaCheckPermission("monitor:server:list")
    @GetMapping("/history")
    public R<List<SysMonitorLog>> getHistory(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        // 默认查询最近1小时
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<SysMonitorLog> list = sysMonitorLogMapper.selectList(
                new LambdaQueryWrapper<SysMonitorLog>()
                        .between(SysMonitorLog::getCreateTime, startTime, endTime)
                        .orderByAsc(SysMonitorLog::getCreateTime)
        );
        return R.ok(list);
    }
}
