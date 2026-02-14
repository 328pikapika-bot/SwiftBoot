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
}
