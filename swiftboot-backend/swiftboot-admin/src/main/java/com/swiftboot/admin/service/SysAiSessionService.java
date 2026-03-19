package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysAiSession;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;
import java.util.Map;

/**
 * 智能会话 Service
 */
public interface SysAiSessionService extends IService<SysAiSession> {

    /**
     * 分页查询会话记录
     */
    Page<SysAiSession> selectAiSessionPage(SysAiSession session, PageQuery pageQuery);

    /**
     * 删除会话记录
     */
    void deleteAiSessionByIds(List<Long> ids);

    /**
     * 清空会话记录
     */
    void cleanAiSession();

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStats(String timeRange, String rankType);

    /**
     * 分页查询用户算力消耗排行
     */
    Page<Map<String, Object>> getUserTokenStats(PageQuery pageQuery, String username, String timeRange, String rankType);
    /**
     * 获取详细的交互活跃度统计数据
     */
    Map<String, Object> getDetailedActivityStats(String timeRange);

    /**
     * 获取详细的算力消耗统计数据
     */
    Map<String, Object> getDetailedTokenStats(String timeRange);

    /**
     * 获取详细的响应延迟统计数据
     */
    Map<String, Object> getDetailedLatencyStats(String timeRange);

    /**
     * 获取知识库与记忆详情统计数据 (左脑硬知识 + 右脑软记忆)
     */
    Map<String, Object> getKnowledgeStats();

    /**
     * 查询历史提问列表
     * 普通用户仅能查看自己的问题，管理员可按需查看所有人的问题
     */
    List<Map<String, Object>> getQuestionHistory(Long currentUserId, boolean allUsers);
}
