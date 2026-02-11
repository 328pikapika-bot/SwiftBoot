package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 操作日志 Service
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志列表
     */
    Page<SysOperLog> selectOperLogPage(SysOperLog operLog, PageQuery pageQuery, String logType);

    /**
     * 删除操作日志
     */
    void deleteOperLogByIds(List<Long> operLogIds);

    /**
     * 清空操作日志
     */
    void cleanOperLog();

    /**
     * 保存操作日志
     */
    void saveOperLog(SysOperLog operLog);

    /**
     * 获取操作模块列表
     */
    List<String> selectOperLogModuleList();
}
