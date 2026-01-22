package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型 Mapper
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
}
