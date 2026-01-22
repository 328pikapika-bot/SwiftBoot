package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysLoginLog;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 登录日志 Service
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     */
    Page<SysLoginLog> selectLoginLogPage(SysLoginLog loginLog, PageQuery pageQuery);

    /**
     * 删除登录日志
     */
    void deleteLoginLogByIds(List<Long> loginLogIds);

    /**
     * 清空登录日志
     */
    void cleanLoginLog();

    /**
     * 保存登录日志
     */
    void saveLoginLog(SysLoginLog loginLog);
}
