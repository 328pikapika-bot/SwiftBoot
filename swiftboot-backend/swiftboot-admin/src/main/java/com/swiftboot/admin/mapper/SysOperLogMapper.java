package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
