package com.swiftboot.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.generator.domain.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码生成表字段 Mapper
 */
@Mapper
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {

    /**
     * 根据表名查询字段列表
     */
    List<GenTableColumn> selectDbTableColumnsByName(@Param("tableName") String tableName);

    /**
     * 根据表ID查询字段列表
     */
    List<GenTableColumn> selectByTableId(@Param("tableId") Long tableId);

    /**
     * 删除表字段
     */
    int deleteByTableId(@Param("tableId") Long tableId);
}
