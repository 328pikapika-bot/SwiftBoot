package com.swiftboot.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.generator.domain.GenTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码生成表 Mapper
 */
@Mapper
public interface GenTableMapper extends BaseMapper<GenTable> {

    /**
     * 查询数据库表列表
     */
    List<GenTable> selectDbTableList(@Param("tableName") String tableName, @Param("tableComment") String tableComment);

    /**
     * 根据表名查询数据库表
     */
    GenTable selectDbTableByName(@Param("tableName") String tableName);

    /**
     * 根据表名列表查询数据库表
     */
    List<GenTable> selectDbTableListByNames(@Param("tableNames") String[] tableNames);
}
