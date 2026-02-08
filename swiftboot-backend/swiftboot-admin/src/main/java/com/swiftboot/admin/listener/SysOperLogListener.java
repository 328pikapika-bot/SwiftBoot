package com.swiftboot.admin.listener;

import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.service.SysOperLogService;
import com.swiftboot.common.log.event.OperLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysOperLogListener {

    private final SysOperLogService operLogService;

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
        operLogService.saveOperLog(operLog);
    }
}
