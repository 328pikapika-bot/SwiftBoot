package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysOperLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 清空操作日志
     */
    void cleanOperLog();

    /**
     * 获取操作模块列表
     */
    @Select("select distinct title from sys_oper_log where title is not null and title != ''")
    List<String> selectOperLogModuleList();
}
