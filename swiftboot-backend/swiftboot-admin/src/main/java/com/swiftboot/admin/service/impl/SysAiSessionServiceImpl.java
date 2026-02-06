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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能会话 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysAiSessionServiceImpl extends ServiceImpl<SysAiSessionMapper, SysAiSession> implements SysAiSessionService {

    @Override
    public Page<SysAiSession> selectAiSessionPage(SysAiSession session, PageQuery pageQuery) {
        Page<SysAiSession> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return baseMapper.selectAiSessionList(page, session.getUserId(), session.getUsername(), session.getQuestion(), session.getModel());
    }

    @Override
    public void deleteAiSessionByIds(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    public void cleanAiSession() {
        remove(new LambdaQueryWrapper<>());
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 今日核心指标
        Map<String, Object> today = baseMapper.selectTodayStats();
        if (today != null) {
            stats.put("todayCount", today.get("today_count"));
            stats.put("todayTokens", today.get("today_tokens"));
            stats.put("avgDuration", today.get("avg_duration"));
        } else {
            stats.put("todayCount", 0);
            stats.put("todayTokens", 0);
            stats.put("avgDuration", 0);
        }

        // 2. Token 趋势
        List<Map<String, Object>> trend = baseMapper.selectTokenTrend();
        stats.put("tokenTrend", trend);

        // 3. 活跃用户
        List<Map<String, Object>> activeUsers = baseMapper.selectActiveUsers();
        stats.put("activeUsers", activeUsers);

        return stats;
    }
}
