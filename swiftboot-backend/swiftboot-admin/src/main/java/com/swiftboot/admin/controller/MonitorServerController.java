package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.swiftboot.admin.domain.server.Server;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 */
@Tag(name = "服务监控")
@RestController
@RequestMapping("/monitor/server")
public class MonitorServerController {

    @Operation(summary = "获取服务器监控信息")
    @SaCheckPermission("monitor:server:list")
    @GetMapping
    public R<Server> getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return R.ok(server);
    }
}
