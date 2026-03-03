package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.SysAiTrace;

/**
 * AI Trace 记录 Service 接口
 *
 * @author swiftboot
 */
public interface SysAiTraceService extends IService<SysAiTrace> {
    
    /**
     * 根据 Trace ID 获取记录
     * 
     * @param traceId 追踪ID
     * @return 追踪记录
     */
    SysAiTrace getByTraceId(String traceId);
    
    /**
     * 根据 Session ID 获取记录
     * 
     * @param sessionId 会话ID
     * @return 追踪记录
     */
    SysAiTrace getBySessionId(Long sessionId);
}
