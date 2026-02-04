package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.mapper.SysOperLogMapper;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.log.event.OperLogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public Page<SysOperLog> selectOperLogPage(SysOperLog operLog, PageQuery pageQuery) {
        Page<SysOperLog> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(operLog.getTitle() != null, SysOperLog::getTitle, operLog.getTitle());
        wrapper.eq(operLog.getBusinessType() != null, SysOperLog::getBusinessType, operLog.getBusinessType());
        wrapper.like(operLog.getOperName() != null, SysOperLog::getOperName, operLog.getOperName());
        wrapper.eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus());
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return page(page, wrapper);
    }

    @Override
    public void deleteOperLogByIds(List<Long> operLogIds) {
        removeByIds(operLogIds);
    }

    @Override
    public void cleanOperLog() {
        baseMapper.cleanOperLog();
    }

    @Override
    public void saveOperLog(SysOperLog operLog) {
        save(operLog);
    }

    @Override
    public List<String> selectOperLogModuleList() {
        return baseMapper.selectOperLogModuleList();
    }

    /**
     * 异步监听操作日志事件
     */
    @Async
    @EventListener
    public void handleOperLogEvent(OperLogEvent event) {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle(event.getTitle());
        operLog.setBusinessType(event.getBusinessType());
        operLog.setMethod(event.getMethod());
        operLog.setRequestMethod(event.getRequestMethod());
        operLog.setOperName(event.getOperName());
        operLog.setOperUrl(event.getOperUrl());
        operLog.setOperIp(event.getOperIp());
        operLog.setOperParam(event.getOperParam());
        operLog.setJsonResult(event.getJsonResult());
        operLog.setStatus(event.getStatus());
        operLog.setErrorMsg(event.getErrorMsg());
        operLog.setOperTime(event.getOperTime());
        operLog.setCostTime(event.getCostTime());
        saveOperLog(operLog);
    }
}
