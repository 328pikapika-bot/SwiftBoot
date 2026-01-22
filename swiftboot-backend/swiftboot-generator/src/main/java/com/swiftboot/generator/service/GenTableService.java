package com.swiftboot.generator.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.generator.domain.GenTable;

import java.util.List;
import java.util.Map;

/**
 * 代码生成 Service
 */
public interface GenTableService extends IService<GenTable> {

    /**
     * 分页查询代码生成表列表
     */
    Page<GenTable> selectGenTablePage(GenTable genTable, PageQuery pageQuery);

    /**
     * 查询数据库表列表
     */
    List<GenTable> selectDbTableList(String tableName, String tableComment);

    /**
     * 根据表名查询数据库表
     */
    GenTable selectDbTableByName(String tableName);

    /**
     * 导入表
     */
    void importTable(String[] tableNames);

    /**
     * 查询表详情（含字段）
     */
    GenTable selectGenTableById(Long tableId);

    /**
     * 修改代码生成信息
     */
    void updateGenTable(GenTable genTable);

    /**
     * 删除代码生成信息
     */
    void deleteGenTableByIds(List<Long> tableIds);

    /**
     * 预览代码
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（下载方式）
     */
    byte[] downloadCode(String tableName);

    /**
     * 生成代码（自定义路径）
     */
    void generateCode(String tableName);

    /**
     * 批量生成代码
     */
    byte[] downloadCode(String[] tableNames);

    /**
     * 同步数据库
     */
    void syncDb(String tableName);
}
