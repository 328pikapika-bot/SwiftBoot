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
     * 统计算力消耗 Top 10 用户
     */
    @Select("SELECT u.username, u.nickname, u.dept_id, d.dept_name, s.user_id, SUM(s.tokens) as tokenConsumption FROM sys_ai_session s LEFT JOIN sys_user u ON s.user_id = u.id LEFT JOIN sys_dept d ON u.dept_id = d.id GROUP BY s.user_id ORDER BY tokenConsumption DESC LIMIT 10")
    List<Map<String, Object>> selectTopTokenUsers();

    /**
     * 分页查询用户算力消耗排行
     */
    @Select("<script>" +
            "SELECT u.id as userId, u.username, u.nickname, u.status, d.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption " +
            "FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "LEFT JOIN sys_ai_session s ON u.id = s.user_id " +
            "WHERE u.del_flag = '0' " +
            "<if test='username != null and username != \"\"'> AND (u.username LIKE concat('%', #{username}, '%') OR u.nickname LIKE concat('%', #{username}, '%'))</if> " +
            "GROUP BY u.id " +
            "ORDER BY tokenConsumption DESC" +
            "</script>")
    Page<Map<String, Object>> selectUserTokenStats(Page<?> page, @Param("username") String username);
}
