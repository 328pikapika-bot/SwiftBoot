package com.swiftboot.admin.task;

import com.swiftboot.admin.domain.monitor.SysMonitorLog;
import com.swiftboot.admin.domain.server.Server;
import com.swiftboot.admin.mapper.SysMonitorLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 监控定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorTask {

    private final SysMonitorLogMapper sysMonitorLogMapper;

    /**
     * 每分钟记录一次服务器状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void recordServerStatus() {
        try {
            Server server = new Server();
            server.copyTo();

            SysMonitorLog log = new SysMonitorLog();
            log.setCpuUsage(server.getCpu().getUsed());
            log.setMemUsage(server.getMem().getUsage());
            log.setJvmUsage(server.getJvm().getUsage());
            log.setCreateTime(LocalDateTime.now());

            sysMonitorLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录服务器监控状态失败", e);
        }
    }
    
    /**
     * 每天凌晨清理数据
     * 策略：
     * 1. 7天内的：保留全部（分钟级）
     * 2. 7-30天的：保留整点（小时级），清理非整点数据
     * 3. 30天前的：全部清理
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearOldData() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 清理30天前的数据
        sysMonitorLogMapper.cleanLogBefore(now.minusDays(30));
        
        // 2. 将7-30天的数据进行降采样（只保留整点）
        // 范围：[30天前, 7天前)
        sysMonitorLogMapper.cleanNonHourlyData(now.minusDays(30), now.minusDays(7));
        
        log.info("执行监控数据清理与降采样完成");
    }
}
