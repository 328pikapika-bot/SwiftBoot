package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.admin.mapper.SysAiSessionMapper;
import com.swiftboot.admin.mapper.SysOperLogMapper;
import com.swiftboot.admin.domain.entity.SysOperLog;
import com.swiftboot.admin.service.SysAiSessionService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.util.ArrayList;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.swiftboot.admin.domain.entity.SysDept;
import com.swiftboot.admin.mapper.SysDeptMapper;
import com.swiftboot.common.security.domain.LoginUser;
import com.swiftboot.common.security.utils.SecurityUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

/**
 * 智能会话 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysAiSessionServiceImpl extends ServiceImpl<SysAiSessionMapper, SysAiSession> implements SysAiSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SysOperLogMapper sysOperLogMapper;
    private final SysDeptMapper sysDeptMapper;
    private static final String MEMORY_DELETE_URL = "http://localhost:8001/memory/delete";
    private static final String STATS_URL = "http://localhost:8001/stats";
    private static final String HISTORY_KEY_PREFIX = "ai:history:";

    // @Override
    public Page<SysAiSession> selectAiSessionPage(SysAiSession session, PageQuery pageQuery) {
        Page<SysAiSession> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return baseMapper.selectAiSessionList(page, session.getUserId(), session.getUsername(), session.getQuestion(), session.getModel(), session.getKeyword());
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
        
        // 获取数据权限过滤条件
        Long deptId = null;
        Long userId = null;
        
        if (!SecurityUtils.isAdmin()) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            // Check if user is a department leader
            SysDept dept = sysDeptMapper.selectById(loginUser.getDeptId());
            boolean isLeader = dept != null && dept.getLeader() != null && 
                             (dept.getLeader().equals(loginUser.getUsername()) || 
                              dept.getLeader().equals(loginUser.getNickname()));
            
            if (isLeader) {
                // Leader: See Department Data
                deptId = loginUser.getDeptId();
            } else {
                // Ordinary Employee: See Own Data
                userId = loginUser.getUserId();
            }
        }

        // 1. 核心指标 (根据 Time Range)
        Map<String, Object> rangeStats = baseMapper.selectStats(timeRange, rankType, deptId, userId);
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
        List<Map<String, Object>> trend = baseMapper.selectTokenTrend(timeRange, deptId, userId);
        stats.put("tokenTrend", trend);

        // 3. 算力消耗排行 (Top 10) - 动态
        List<Map<String, Object>> topUsers = baseMapper.selectTopStats(timeRange, rankType, deptId, userId);
        stats.put("topUsers", topUsers);
        
        // 4. 雷达图数据 (认知能力分析) - 实时计算
        Map<String, Object> radar = new HashMap<>();
        
        // 解析时间范围
        LocalDateTime start = null;
        LocalDateTime end = null;
        LocalDateTime now = LocalDateTime.now();
        int targetActivity = 5000;
        
        if ("day".equals(timeRange) || "today".equals(timeRange)) {
            start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            targetActivity = 50;
        } else if ("week".equals(timeRange)) {
            start = now.with(DayOfWeek.MONDAY).with(LocalTime.MIN);
            end = now.with(DayOfWeek.SUNDAY).with(LocalTime.MAX);
            targetActivity = 300;
        } else if ("month".equals(timeRange)) {
            start = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
            end = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
            targetActivity = 1000;
        }
        
        // 4.1 知识储备 (Knowledge) - 从 Python 引擎获取
        int knowledgeScore = 0;
        try {
            String result = HttpRequest.get(STATS_URL).timeout(1000).execute().body();
            if (result != null) {
                JSONObject json = new JSONObject(result);
                int count = json.getInt("knowledge_count", 0);
                // 假设 1000 条切片为满分 100
                knowledgeScore = Math.min(100, (int) ((count / 1000.0) * 100));
            }
        } catch (Exception e) {
            knowledgeScore = 60; // 默认分
        }
        radar.put("knowledge", knowledgeScore);
        
        // 4.2 交互活跃 (Activity) - 基于总会话数
        LambdaQueryWrapper<SysAiSession> activityQuery = new LambdaQueryWrapper<>();
        if (start != null) {
            activityQuery.ge(SysAiSession::getCreateTime, start).le(SysAiSession::getCreateTime, end);
        }
        Long totalSessions = baseMapper.selectCount(activityQuery);
        // 基于时间范围的动态目标
        int activityScore = Math.min(100, (int) ((totalSessions / (double)targetActivity) * 100));
        radar.put("activity", Math.max(20, activityScore)); // 给个保底分
        
        // 4.3 记忆深度 (Memory) - 模拟 (人均对话轮数)
        // 简单算法：总会话数 / (活跃用户数 + 1) * 权重
        // 如果是按天/周/月，只计算该时间段内的活跃用户
        int memoryScore = 75; 
        try {
             // 由于无法直接通过 selectCount(distinct) 获取用户数，这里简化处理：
             // 如果会话数很少，假设用户数也很少。
             // 如果会话数 > 0，则尝试计算
             if (totalSessions > 0) {
                 // 估算：假设平均每人 5 次会话
                 // 实际上应该查询 distinct user_id，但为了性能暂时简化，或者查询 ID 列表（如果量不大）
                 // 这里采用更准确的方式：查询该时间段内参与的用户ID数量
                 // 注意：如果数据量非常大，selectObjs 可能会有性能问题，但对于 Dashboard 来说通常带时间范围，数据量可控
                 List<Object> userIds = baseMapper.selectObjs(new LambdaQueryWrapper<SysAiSession>()
                     .select(SysAiSession::getUserId)
                     .ge(start != null, SysAiSession::getCreateTime, start)
                     .le(end != null, SysAiSession::getCreateTime, end));
                 
                 long uniqueUsers = userIds.stream().distinct().count();
                 if (uniqueUsers > 0) {
                     double avgSessions = (double) totalSessions / uniqueUsers;
                     // 假设平均 5 次会话为满分 (100分)
                     memoryScore = Math.min(100, (int) (avgSessions / 5.0 * 100));
                 }
             }
        } catch (Exception e) {
            // ignore
        }
        radar.put("memory", Math.max(40, memoryScore));
        
        // 4.4 响应速度 (Speed) - 基于 sys_oper_log
        int speedScore = 80;
        try {
            // 查询最近 50 条 AI 接口调用的平均耗时 -> 改为查询时间段内的平均耗时
            LambdaQueryWrapper<SysOperLog> logQuery = new LambdaQueryWrapper<SysOperLog>()
                .like(SysOperLog::getTitle, "智能")
                .ge(start != null, SysOperLog::getOperTime, start)
                .le(end != null, SysOperLog::getOperTime, end)
                .orderByDesc(SysOperLog::getOperTime);
                
            // 如果是 total，限制 100 条以免全表扫描
            if (start == null) {
                logQuery.last("LIMIT 100");
            }
                
            List<SysOperLog> logs = sysOperLogMapper.selectList(logQuery);
            
            if (!logs.isEmpty()) {
                double avgCost = logs.stream().mapToLong(SysOperLog::getCostTime).average().orElse(0);
                // 假设 3秒(3000ms) = 60分, 1秒(1000ms) = 90分
                // 算法: 100 - (avgCost / 100)
                speedScore = Math.max(0, Math.min(100, 100 - (int)(avgCost / 100)));
            }
        } catch (Exception e) {
            // ignore
        }
        radar.put("speed", speedScore);
        
        // 4.5 技能覆盖 (Skills) - 固定值 (目前已有 RAG, Chat, Search 等)
        radar.put("skills", 85);
        
        // 4.6 准确度 (Accuracy) - 固定值 (DeepSeek V3 表现优异)
        radar.put("accuracy", 92);
        
        stats.put("radar", radar);

        // 5. 热点词云 (Word Cloud) - 动态
        List<Map<String, Object>> wordCloud = baseMapper.selectWordCloudStats(timeRange, deptId, userId);
        stats.put("wordCloud", wordCloud);

        return stats;
    }

    // @Override
    public Page<Map<String, Object>> getUserTokenStats(PageQuery pageQuery, String username, String timeRange, String rankType) {
        Page<Map<String, Object>> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        
        Long deptId = null;
        Long userId = null;
        
        if (!SecurityUtils.isAdmin()) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            // Check if user is a department leader
            SysDept dept = sysDeptMapper.selectById(loginUser.getDeptId());
            boolean isLeader = dept != null && dept.getLeader() != null && 
                             (dept.getLeader().equals(loginUser.getUsername()) || 
                              dept.getLeader().equals(loginUser.getNickname()));
            
            if (isLeader) {
                // Leader: See Department Data
                deptId = loginUser.getDeptId();
            } else {
                // Ordinary Employee: See Own Data
                userId = loginUser.getUserId();
            }
        }
        
        return baseMapper.selectUserTokenStats(page, username, timeRange, rankType, deptId, userId);
    }
}
