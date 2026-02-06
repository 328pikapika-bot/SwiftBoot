package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.entity.SysAiSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 智能会话 Mapper
 */
@Mapper
public interface SysAiSessionMapper extends BaseMapper<SysAiSession> {

    /**
     * 查询会话列表（关联用户表）
     */
    Page<SysAiSession> selectAiSessionList(Page<SysAiSession> page, @Param("userId") Long userId, 
                                         @Param("username") String username, 
                                         @Param("question") String question, 
                                         @Param("model") String model);

    /**
     * 统计今日数据
     */
    @Select("SELECT COUNT(*) as today_count, COALESCE(SUM(tokens), 0) as today_tokens, COALESCE(AVG(duration), 0) as avg_duration FROM sys_ai_session WHERE DATE(create_time) = CURDATE()")
    Map<String, Object> selectTodayStats();

    /**
     * 统计近7天 Token 消耗趋势
     */
    @Select("SELECT DATE(create_time) as date, SUM(tokens) as tokens FROM sys_ai_session WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectTokenTrend();

    /**
     * 统计活跃用户 Top 10
     */
    @Select("SELECT u.username, COUNT(*) as count FROM sys_ai_session s LEFT JOIN sys_user u ON s.user_id = u.id GROUP BY s.user_id ORDER BY count DESC LIMIT 10")
    List<Map<String, Object>> selectActiveUsers();
}
