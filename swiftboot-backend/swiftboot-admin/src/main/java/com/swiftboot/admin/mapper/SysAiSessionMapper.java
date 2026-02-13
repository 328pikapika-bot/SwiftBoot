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
                                         @Param("model") String model,
                                         @Param("keyword") String keyword);

    /**
     * 统计范围数据
     */
    @Select("<script>" +
            "SELECT COUNT(*) as count, COALESCE(SUM(tokens), 0) as tokens, COALESCE(AVG(duration), 0) as avg_duration " +
            "FROM sys_ai_session s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "WHERE u.deleted = 0 " +
            "<if test='userId != null'> AND s.user_id = #{userId} </if>" +
            "<if test='deptId != null'> AND (u.dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, d.ancestors)) </if>" +
            "<if test='timeRange == \"day\"'> AND DATE(s.create_time) = CURDATE() </if>" +
            "<if test='timeRange == \"week\"'> AND YEARWEEK(s.create_time, 1) = YEARWEEK(CURDATE(), 1) </if>" +
            "<if test='timeRange == \"month\"'> AND DATE_FORMAT(s.create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') </if>" +
            "</script>")
    Map<String, Object> selectStats(@Param("timeRange") String timeRange, @Param("rankType") String rankType, @Param("deptId") Long deptId, @Param("userId") Long userId);

    /**
     * 统计近7天/周/月 Token 消耗趋势
     */
    @Select("<script>" +
            "SELECT " +
            "<choose>" +
            "  <when test='timeRange == \"month\"'>DATE_FORMAT(s.create_time, '%Y-%m-01')</when>" +
            "  <when test='timeRange == \"week\"'>DATE_FORMAT(DATE_SUB(s.create_time, INTERVAL WEEKDAY(s.create_time) DAY), '%Y-%m-%d')</when>" +
            "  <otherwise>DATE_FORMAT(s.create_time, '%Y-%m-%d')</otherwise>" +
            "</choose>" +
            " as date, SUM(s.tokens) as tokens " +
            "FROM sys_ai_session s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND s.user_id = #{userId} </if>" +
            "<if test='deptId != null'> AND (u.dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, d.ancestors)) </if>" +
            "<choose>" +
            "  <when test='timeRange == \"month\"'> AND s.create_time >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) </when>" +
            "  <when test='timeRange == \"week\"'> AND s.create_time >= DATE_SUB(CURDATE(), INTERVAL 6 WEEK) </when>" +
            "  <otherwise> AND s.create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) </otherwise>" +
            "</choose>" +
            "GROUP BY date " +
            "ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectTokenTrend(@Param("timeRange") String timeRange, @Param("deptId") Long deptId, @Param("userId") Long userId);

    /**
     * 统计算力消耗 Top 10
     */
    @Select("<script>" +
            "SELECT " +
            "<choose>" +
            "  <when test='rankType == \"dept_all\"'>d.id as deptId, d.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption</when>" +
            "  <when test='rankType == \"dept_level1\"'>l1.id as deptId, l1.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption</when>" +
            "  <otherwise>u.username, u.nickname, s.user_id, COALESCE(SUM(s.tokens), 0) as tokenConsumption, d.dept_name as deptName</otherwise>" +
            "</choose>" +
            " FROM sys_ai_session s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "<if test='rankType == \"dept_level1\"'>" +
            "  LEFT JOIN sys_dept l1 ON l1.id = (CASE WHEN d.parent_id = 0 THEN d.id ELSE CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(d.ancestors, ',', 2), ',', -1) AS UNSIGNED) END) " +
            "</if>" +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND s.user_id = #{userId} </if>" +
            "<if test='deptId != null'> AND (u.dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, d.ancestors)) </if>" +
            "<if test='timeRange == \"day\"'> AND DATE(s.create_time) = CURDATE() </if>" +
            "<if test='timeRange == \"week\"'> AND YEARWEEK(s.create_time, 1) = YEARWEEK(CURDATE(), 1) </if>" +
            "<if test='timeRange == \"month\"'> AND DATE_FORMAT(s.create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') </if>" +
            "GROUP BY " +
            "<choose>" +
            "  <when test='rankType == \"dept_all\"'>d.id</when>" +
            "  <when test='rankType == \"dept_level1\"'>l1.id</when>" +
            "  <otherwise>s.user_id</otherwise>" +
            "</choose>" +
            " ORDER BY tokenConsumption DESC LIMIT 10" +
            "</script>")
    List<Map<String, Object>> selectTopStats(@Param("timeRange") String timeRange, @Param("rankType") String rankType, @Param("deptId") Long deptId, @Param("userId") Long userId);

    /**
     * 分页查询用户算力消耗排行
     */
    @Select("<script>" +
            "SELECT " +
            "<choose>" +
            "  <when test='rankType == \"dept_all\"'>d.id as deptId, d.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption</when>" +
            "  <when test='rankType == \"dept_level1\"'>l1.id as deptId, l1.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption</when>" +
            "  <otherwise>u.id as userId, u.username, u.nickname, u.status, d.dept_name as deptName, COALESCE(SUM(s.tokens), 0) as tokenConsumption</otherwise>" +
            "</choose>" +
            " FROM sys_user u " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "<if test='rankType == \"dept_level1\"'>" +
            "  LEFT JOIN sys_dept l1 ON l1.id = (CASE WHEN d.parent_id = 0 THEN d.id ELSE CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(d.ancestors, ',', 2), ',', -1) AS UNSIGNED) END) " +
            "</if>" +
            "LEFT JOIN sys_ai_session s ON u.id = s.user_id " +
            "<if test='timeRange == \"day\"'> AND DATE(s.create_time) = CURDATE() </if>" +
            "<if test='timeRange == \"week\"'> AND YEARWEEK(s.create_time, 1) = YEARWEEK(CURDATE(), 1) </if>" +
            "<if test='timeRange == \"month\"'> AND DATE_FORMAT(s.create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') </if>" +
            "WHERE u.deleted = 0 " +
            "<if test='userId != null'> AND s.user_id = #{userId} </if>" +
            "<if test='deptId != null'> AND (u.dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, d.ancestors)) </if>" +
            "<if test='username != null and username != \"\"'> AND (u.username LIKE concat('%', #{username}, '%') OR u.nickname LIKE concat('%', #{username}, '%'))</if> " +
            "GROUP BY " +
            "<choose>" +
            "  <when test='rankType == \"dept_all\"'>d.id</when>" +
            "  <when test='rankType == \"dept_level1\"'>l1.id</when>" +
            "  <otherwise>u.id</otherwise>" +
            "</choose>" +
            " ORDER BY tokenConsumption DESC" +
            "</script>")
    Page<Map<String, Object>> selectUserTokenStats(Page<?> page, @Param("username") String username, @Param("timeRange") String timeRange, @Param("rankType") String rankType, @Param("deptId") Long deptId, @Param("userId") Long userId);

    /**
     * 统计热点词云数据
     */
    @Select("<script>" +
            "SELECT " +
            "topic as name, " +
            "COUNT(*) as value, " +
            "FLOOR(RAND() * 6) as category " +
            "FROM sys_ai_session s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_dept d ON u.dept_id = d.id " +
            "WHERE s.topic IS NOT NULL AND s.topic != '' " +
            "<if test='userId != null'> AND s.user_id = #{userId} </if>" +
            "<if test='deptId != null'> AND (u.dept_id = #{deptId} OR FIND_IN_SET(#{deptId}, d.ancestors)) </if>" +
            "<if test='timeRange == \"day\"'> AND DATE(s.create_time) = CURDATE() </if>" +
            "<if test='timeRange == \"week\"'> AND YEARWEEK(s.create_time, 1) = YEARWEEK(CURDATE(), 1) </if>" +
            "<if test='timeRange == \"month\"'> AND DATE_FORMAT(s.create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') </if>" +
            "GROUP BY topic " +
            "ORDER BY value DESC " +
            "LIMIT 30" +
            "</script>")
    List<Map<String, Object>> selectWordCloudStats(@Param("timeRange") String timeRange, @Param("deptId") Long deptId, @Param("userId") Long userId);
}
