package com.swiftboot.admin.context;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI Trace 上下文持有者
 * 用于在一次请求中收集 AI 执行链路信息
 *
 * @author swiftboot
 */
public class AiTraceContext {

    private static final ThreadLocal<AiTraceData> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 初始化上下文
     */
    public static void init() {
        AiTraceData data = new AiTraceData();
        data.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        data.setStartTime(System.currentTimeMillis());
        data.setThoughtPath(new ArrayList<>());
        data.setToolCalls(new ArrayList<>());
        data.setMemoryHitIds(new ArrayList<>());
        THREAD_LOCAL.set(data);
    }

    /**
     * 获取当前上下文数据
     */
    public static AiTraceData get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 获取当前 Trace ID
     */
    public static String getTraceId() {
        AiTraceData data = get();
        return data != null ? data.getTraceId() : null;
    }

    /**
     * 添加思考路径
     */
    public static void addThought(String thought) {
        AiTraceData data = get();
        if (data != null) {
            data.getThoughtPath().add(thought);
        }
    }

    /**
     * 添加工具调用记录
     */
    public static void addToolCall(String toolName, Object params, Object result, long duration) {
        AiTraceData data = get();
        if (data != null) {
            JSONObject call = new JSONObject();
            call.set("tool", toolName);
            call.set("params", params);
            call.set("result", result);
            call.set("duration", duration);
            call.set("timestamp", System.currentTimeMillis());
            data.getToolCalls().add(call);
        }
    }

    /**
     * 设置上下文信息（检索结果）
     */
    public static void setContextInfo(String info) {
        AiTraceData data = get();
        if (data != null) {
            data.setContextInfo(info);
        }
    }

    /**
     * 设置命中的历史记忆 ID
     */
    public static void setMemoryHitIds(List<String> memoryHitIds) {
        AiTraceData data = get();
        if (data != null) {
            data.setMemoryHitIds(memoryHitIds);
        }
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

    /**
     * 内部数据类
     */
    @Data
    public static class AiTraceData {
        private String traceId;
        private Long startTime;
        private List<String> thoughtPath;
        private List<JSONObject> toolCalls;
        private String contextInfo;
        private List<String> memoryHitIds;
    }
}
