package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.monitor.SysMonitorLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 监控日志 Mapper
 */
@Mapper
public interface SysMonitorLogMapper extends BaseMapper<SysMonitorLog> {

    /**
     * 清理指定时间之前的数据
     */
    @Delete("DELETE FROM sys_monitor_log WHERE create_time < #{time}")
    void cleanLogBefore(@Param("time") LocalDateTime time);

    /**
     * 清理指定时间范围内，非整点的数据（保留整点数据作为降采样）
     * 保留 create_time 分钟数为 00 的记录
     */
    @Delete("DELETE FROM sys_monitor_log WHERE create_time < #{endTime} AND create_time >= #{startTime} AND MINUTE(create_time) != 0")
    void cleanNonHourlyData(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
