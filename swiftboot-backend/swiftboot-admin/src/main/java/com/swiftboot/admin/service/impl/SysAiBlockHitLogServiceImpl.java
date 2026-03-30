package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysAiBlockHitLog;
import com.swiftboot.admin.mapper.SysAiBlockHitLogMapper;
import com.swiftboot.admin.service.SysAiBlockHitLogService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI block word hit log service implementation.
 */
@Service
@RequiredArgsConstructor
public class SysAiBlockHitLogServiceImpl implements SysAiBlockHitLogService {

    private final SysAiBlockHitLogMapper blockHitLogMapper;

    @Override
    public Page<SysAiBlockHitLog> selectPage(SysAiBlockHitLog query, PageQuery pageQuery) {
        Page<SysAiBlockHitLog> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysAiBlockHitLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getCategoryName() != null && !query.getCategoryName().isBlank(), SysAiBlockHitLog::getCategoryName, query.getCategoryName())
                .like(query.getWordText() != null && !query.getWordText().isBlank(), SysAiBlockHitLog::getWordText, query.getWordText())
                .like(query.getUsername() != null && !query.getUsername().isBlank(), SysAiBlockHitLog::getUsername, query.getUsername())
                .like(query.getQuestionContent() != null && !query.getQuestionContent().isBlank(), SysAiBlockHitLog::getQuestionContent, query.getQuestionContent())
                .orderByDesc(SysAiBlockHitLog::getCreateTime);
        return blockHitLogMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStart = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long totalCount = blockHitLogMapper.selectCount(new LambdaQueryWrapper<>());
        long todayCount = blockHitLogMapper.selectCount(new LambdaQueryWrapper<SysAiBlockHitLog>()
                .ge(SysAiBlockHitLog::getCreateTime, todayStart));
        long weekCount = blockHitLogMapper.selectCount(new LambdaQueryWrapper<SysAiBlockHitLog>()
                .ge(SysAiBlockHitLog::getCreateTime, weekStart));

        List<SysAiBlockHitLog> recentLogs = blockHitLogMapper.selectList(new LambdaQueryWrapper<SysAiBlockHitLog>()
                .ge(SysAiBlockHitLog::getCreateTime, weekStart)
                .orderByDesc(SysAiBlockHitLog::getCreateTime));

        String topCategory = recentLogs.stream()
                .collect(Collectors.groupingBy(SysAiBlockHitLog::getCategoryName, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        String topWord = recentLogs.stream()
                .collect(Collectors.groupingBy(SysAiBlockHitLog::getWordText, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        List<Map<String, Object>> trend = recentLogs.stream()
                .filter(log -> log.getCreateTime() != null)
                .collect(Collectors.groupingBy(log -> log.getCreateTime().toLocalDate().toString(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .toList();

        stats.put("totalCount", totalCount);
        stats.put("todayCount", todayCount);
        stats.put("weekCount", weekCount);
        stats.put("topCategory", topCategory);
        stats.put("topWord", topWord);
        stats.put("trend", trend);
        stats.put("latestHitAt", recentLogs.stream().map(SysAiBlockHitLog::getCreateTime).filter(Objects::nonNull).findFirst().orElse(null));
        return stats;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordHit(SysAiBlockHitLog log) {
        blockHitLogMapper.insert(log);
    }
}
