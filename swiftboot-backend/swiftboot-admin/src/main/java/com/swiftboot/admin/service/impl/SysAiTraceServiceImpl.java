package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.SysAiTrace;
import com.swiftboot.admin.mapper.SysAiTraceMapper;
import com.swiftboot.admin.service.SysAiTraceService;
import org.springframework.stereotype.Service;

/**
 * AI Trace 记录 Service 实现类
 *
 * @author swiftboot
 */
@Service
public class SysAiTraceServiceImpl extends ServiceImpl<SysAiTraceMapper, SysAiTrace> implements SysAiTraceService {

    @Override
    public SysAiTrace getByTraceId(String traceId) {
        return this.getOne(new LambdaQueryWrapper<SysAiTrace>()
                .eq(SysAiTrace::getTraceId, traceId));
    }

    @Override
    public SysAiTrace getBySessionId(Long sessionId) {
        return this.getOne(new LambdaQueryWrapper<SysAiTrace>()
                .eq(SysAiTrace::getSessionId, sessionId));
    }
}
