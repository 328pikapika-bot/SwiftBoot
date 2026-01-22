package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysLoginLog;
import com.swiftboot.admin.mapper.SysLoginLogMapper;
import com.swiftboot.admin.service.SysLoginLogService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public Page<SysLoginLog> selectLoginLogPage(SysLoginLog loginLog, PageQuery pageQuery) {
        Page<SysLoginLog> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(loginLog.getUsername() != null, SysLoginLog::getUsername, loginLog.getUsername());
        wrapper.like(loginLog.getLoginIp() != null, SysLoginLog::getLoginIp, loginLog.getLoginIp());
        wrapper.eq(loginLog.getStatus() != null, SysLoginLog::getStatus, loginLog.getStatus());
        wrapper.orderByDesc(SysLoginLog::getLoginTime);
        return page(page, wrapper);
    }

    @Override
    public void deleteLoginLogByIds(List<Long> loginLogIds) {
        removeByIds(loginLogIds);
    }

    @Override
    public void cleanLoginLog() {
        baseMapper.cleanLoginLog();
    }

    @Override
    public void saveLoginLog(SysLoginLog loginLog) {
        save(loginLog);
    }
}
