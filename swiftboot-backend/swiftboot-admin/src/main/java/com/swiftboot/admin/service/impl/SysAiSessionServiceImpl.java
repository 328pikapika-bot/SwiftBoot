package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.admin.mapper.SysAiSessionMapper;
import com.swiftboot.admin.service.SysAiSessionService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.util.ArrayList;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能会话 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysAiSessionServiceImpl extends ServiceImpl<SysAiSessionMapper, SysAiSession> implements SysAiSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String MEMORY_DELETE_URL = "http://localhost:8001/memory/delete";
    private static final String HISTORY_KEY_PREFIX = "ai:history:";

    @Override
    public Page<SysAiSession> selectAiSessionPage(SysAiSession session, PageQuery pageQuery) {
        Page<SysAiSession> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return baseMapper.selectAiSessionList(page, session.getUserId(), session.getUsername(), session.getQuestion(), session.getModel());
    }

    @Override
    public void deleteAiSessionByIds(List<Long> ids) {
        // 1. 获取要删除的会话记录
        List<SysAiSession> sessions = listByIds(ids);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // 2. 尝试删除向量库记忆 (Grouping by User)
        Map<Long, List<String>> userMessagesMap = new HashMap<>();
        for (SysAiSession session : sessions) {
            userMessagesMap.computeIfAbsent(session.getUserId(), k -> new ArrayList<>()).add(session.getQuestion());
            // Answer 也应该删除
            if (session.getAnswer() != null) {
                userMessagesMap.get(session.getUserId()).add(session.getAnswer());
            }
// ...

    // --- 新增：删除 Redis 短期记忆 (增强版) ---
    try {
        String redisKey = HISTORY_KEY_PREFIX + session.getUserId();
        
        // 1. 获取所有 Redis 记录
        List<String> historyStr = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        if (historyStr != null) {
            // 2. 在内存中查找匹配的原始字符串
            List<String> toRemove = new ArrayList<>();
            for (String rawJson : historyStr) {
                try {
                    JSONObject json = new JSONObject(rawJson);
                    String content = json.getStr("content");
                    String role = json.getStr("role");
                    
                    // 匹配 Question
                    if ("user".equals(role) && session.getQuestion().equals(content)) {
                        toRemove.add(rawJson);
                    }
                    // 匹配 Answer
                    if ("assistant".equals(role) && session.getAnswer() != null && session.getAnswer().equals(content)) {
                        toRemove.add(rawJson);
                    }
                } catch (Exception ignored) {}
            }
            
            // 3. 执行删除
            for (String raw : toRemove) {
                if (raw != null) {
                    stringRedisTemplate.opsForList().remove(redisKey, 0, (Object) raw);
                    System.out.println("从Redis历史记录中移除： " + raw);
                }
            }
        }
    } catch (Exception e) {
        System.err.println("未能删除用户的Redis内存 " + session.getUserId() + ": " + e.getMessage());
    }
    // -------------------------------
        }

        // 3. 调用 Python 引擎删除
        for (Map.Entry<Long, List<String>> entry : userMessagesMap.entrySet()) {
            try {
                JSONObject body = new JSONObject();
                body.set("user_id", String.valueOf(entry.getKey()));
                body.set("messages", new JSONArray(entry.getValue()));

                // 打印待删除内容预览
                System.out.println("Attempting to delete memories for user " + entry.getKey() + ":");
                for (String msg : entry.getValue()) {
                    String preview = msg.length() > 50 ? msg.substring(0, 50) + "..." : msg;
                    System.out.println("  - " + preview.replace("\n", " "));
                }
                
                String resp = HttpRequest.post(MEMORY_DELETE_URL)
                        .timeout(3000)
                        .body(body.toString())
                        .execute()
                        .body();
                
                // 打印删除结果日志
                JSONObject respJson = new JSONObject(resp);
                if (respJson.containsKey("deleted_count")) {
                    System.out.println("Deleted " + respJson.getInt("deleted_count") + " memories from Vector DB for user " + entry.getKey());
                }
            } catch (Exception e) {
                // 仅打印错误，不影响主流程
                System.err.println("未能为用户删除内存 " + entry.getKey() + ": " + e.getMessage());
            }
        }

        // 4. 删除数据库记录
        removeByIds(ids);
    }

    @Override
    public void cleanAiSession() {
        remove(new LambdaQueryWrapper<>());
    }

    @Override
    public Map<String, Object> getDashboardStats(String timeRange, String rankType) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 核心指标 (根据 Time Range)
        Map<String, Object> rangeStats = baseMapper.selectStats(timeRange, rankType);
        if (rangeStats != null) {
            stats.put("todayCount", rangeStats.get("count")); // 保持前端字段名兼容，实际含义为 Current Range Count
            stats.put("todayTokens", rangeStats.get("tokens"));
            stats.put("avgDuration", rangeStats.get("avg_duration"));
        } else {
            stats.put("todayCount", 0);
            stats.put("todayTokens", 0);
            stats.put("avgDuration", 0);
        }

        // 2. Token 趋势 (根据 timeRange 动态变化)
        List<Map<String, Object>> trend = baseMapper.selectTokenTrend(timeRange);
        stats.put("tokenTrend", trend);

        // 3. 算力消耗排行 (Top 10) - 动态
        List<Map<String, Object>> topUsers = baseMapper.selectTopStats(timeRange, rankType);
        stats.put("topUsers", topUsers);

        return stats;
    }

    @Override
    public Page<Map<String, Object>> getUserTokenStats(PageQuery pageQuery, String username, String timeRange, String rankType) {
        Page<Map<String, Object>> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return baseMapper.selectUserTokenStats(page, username, timeRange, rankType);
    }
}
