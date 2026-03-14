package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时任务 Mapper
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
    List<SysJob> selectJobList(SysJob job);
}
