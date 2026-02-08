package com.swiftboot.admin.domain.monitor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务器监控日志
 */
@Data
@TableName("sys_monitor_log")
public class SysMonitorLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    private Double memUsage;

    /**
     * JVM使用率
     */
    private Double jvmUsage;

    /**
     * 记录时间
     */
    private LocalDateTime createTime;
}
