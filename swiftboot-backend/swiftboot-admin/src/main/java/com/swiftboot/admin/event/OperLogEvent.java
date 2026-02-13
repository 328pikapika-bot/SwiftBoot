package com.swiftboot.admin.event;

import com.swiftboot.admin.domain.entity.SysOperLog;
import org.springframework.context.ApplicationEvent;

public class OperLogEvent extends ApplicationEvent {
    private final SysOperLog operLog;

    public OperLogEvent(Object source, SysOperLog operLog) {
        super(source);
        this.operLog = operLog;
    }

    public SysOperLog getOperLog() {
        return operLog;
    }
}
