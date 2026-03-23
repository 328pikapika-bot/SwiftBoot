package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.SysAiTrace;
import com.swiftboot.admin.service.SysAiTraceService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import com.swiftboot.common.core.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Trace 记录 Controller
 *
 * @author swiftboot
 */
@RestController
@RequestMapping("/system/ai/trace")
public class SysAiTraceController {

    @Autowired
    private SysAiTraceService sysAiTraceService;

    /**
     * 获取 Trace 详情
     */
    @GetMapping("/{traceId}")
    public R<SysAiTrace> getInfo(@PathVariable("traceId") String traceId) {
        SysAiTrace trace = sysAiTraceService.getByTraceId(traceId);
        if (trace == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Trace record not found");
        }
        return R.ok(trace);
    }

    /**
     * 根据 Session ID 获取 Trace 详情
     */
    @GetMapping("/session/{sessionId}")
    public R<SysAiTrace> getBySessionId(@PathVariable("sessionId") Long sessionId) {
        SysAiTrace trace = sysAiTraceService.getBySessionId(sessionId);
        // 如果找不到，返回空数据即可，不报错
        return R.ok(trace);
    }
}
