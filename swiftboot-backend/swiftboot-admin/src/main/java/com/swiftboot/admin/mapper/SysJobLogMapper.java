package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时任务日志 Mapper
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
    List<SysJobLog> selectJobLogList(SysJobLog log);
}
