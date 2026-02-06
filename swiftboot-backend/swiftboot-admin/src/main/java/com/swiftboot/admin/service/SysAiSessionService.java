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
    Map<String, Object> getDashboardStats();
}
