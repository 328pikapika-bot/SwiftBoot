package com.swiftboot.generator.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.generator.domain.GenTable;
import com.swiftboot.generator.domain.GenTableColumn;
import com.swiftboot.generator.mapper.GenTableColumnMapper;
import com.swiftboot.generator.mapper.GenTableMapper;
import com.swiftboot.generator.service.GenTableService;
import com.swiftboot.generator.util.GenUtils;
import com.swiftboot.generator.util.VelocityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements GenTableService {

    private final GenTableColumnMapper columnMapper;

    @Override
    public Page<GenTable> selectGenTablePage(GenTable genTable, PageQuery pageQuery) {
        Page<GenTable> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<GenTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(genTable.getTableName() != null, GenTable::getTableName, genTable.getTableName());
        wrapper.like(genTable.getTableComment() != null, GenTable::getTableComment, genTable.getTableComment());
        wrapper.orderByDesc(GenTable::getUpdateTime);
        return page(page, wrapper);
    }

    @Override
    public List<GenTable> selectDbTableList(String tableName, String tableComment) {
        return baseMapper.selectDbTableList(tableName, tableComment);
    }

    @Override
    public GenTable selectDbTableByName(String tableName) {
        return baseMapper.selectDbTableByName(tableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTable(String[] tableNames) {
        List<GenTable> tables = baseMapper.selectDbTableListByNames(tableNames);
        for (GenTable table : tables) {
            String tableName = table.getTableName();
            GenUtils.initTable(table);
            table.setCreateTime(LocalDateTime.now());
            table.setUpdateTime(LocalDateTime.now());
            save(table);

            // 保存字段信息
            List<GenTableColumn> columns = columnMapper.selectDbTableColumnsByName(tableName);
            for (GenTableColumn column : columns) {
                GenUtils.initColumnField(column, table);
                column.setTableId(table.getId());
                columnMapper.insert(column);
            }
        }
    }

    @Override
    public GenTable selectGenTableById(Long tableId) {
        GenTable table = getById(tableId);
        if (table != null) {
            table.setColumns(columnMapper.selectByTableId(tableId));
            table.setPkColumn(table.getColumns().stream()
                    .filter(c -> "1".equals(c.getIsPk()))
                    .findFirst()
                    .orElse(null));
        }
        return table;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGenTable(GenTable genTable) {
        genTable.setUpdateTime(LocalDateTime.now());
        updateById(genTable);

        // 更新字段信息
        if (genTable.getColumns() != null) {
            for (GenTableColumn column : genTable.getColumns()) {
                columnMapper.updateById(column);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGenTableByIds(List<Long> tableIds) {
        removeByIds(tableIds);
        for (Long tableId : tableIds) {
            columnMapper.deleteByTableId(tableId);
        }
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        GenTable table = selectGenTableById(tableId);
        if (table == null) {
            throw new BusinessException("表不存在");
        }

        VelocityUtils.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);

        List<String> templates = VelocityUtils.getTemplateList();
        for (String template : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    @Override
    public byte[] downloadCode(String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generateCode(tableName, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    @Override
    public void generateCode(String tableName) {
        GenTable table = lambdaQuery().eq(GenTable::getTableName, tableName).one();
        if (table == null) {
            throw new BusinessException("表不存在");
        }
        table = selectGenTableById(table.getId());

        VelocityUtils.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);

        List<String> templates = VelocityUtils.getTemplateList();
        for (String template : templates) {
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
                tpl.merge(context, sw);

                String path = VelocityUtils.getFileName(template, table);
                if (StrUtil.isNotBlank(table.getGenPath())) {
                    // 生成到自定义路径
                    try {
                        java.io.File file = new java.io.File(table.getGenPath() + "/" + path);
                        java.io.File dir = file.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        java.io.FileWriter fw = new java.io.FileWriter(file);
                        fw.write(sw.toString());
                        fw.close();
                    } catch (IOException e) {
                        log.error("生成代码失败", e);
                        throw new BusinessException("生成代码失败: " + e.getMessage());
                    }
                }
            }
        }

    /**
     * 获取前端项目路径
     */
    private String getFrontendPath() {
        // 从当前工作目录向上查找前端项目
        String currentPath = System.getProperty("user.dir");
        java.io.File currentDir = new java.io.File(currentPath);

        // 向上查找 swiftboot-ui 目录
        while (currentDir != null) {
            java.io.File uiDir = new java.io.File(currentDir, "swiftboot-ui");
            if (uiDir.exists() && uiDir.isDirectory()) {
                return uiDir.getAbsolutePath();
            }
            currentDir = currentDir.getParentFile();
        }

        // 如果没找到，尝试相对路径
        java.io.File relativeUiDir = new java.io.File(currentPath, "../swiftboot-ui");
        if (relativeUiDir.exists() && relativeUiDir.isDirectory()) {
            return relativeUiDir.getAbsolutePath();
        }

        return null;
    }

    @Override
    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : tableNames) {
            generateCode(tableName, zip);
        }
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDb(String tableName) {
        GenTable table = lambdaQuery().eq(GenTable::getTableName, tableName).one();
        if (table == null) {
            throw new BusinessException("表不存在");
        }

        List<GenTableColumn> dbColumns = columnMapper.selectDbTableColumnsByName(tableName);
        List<GenTableColumn> tableColumns = columnMapper.selectByTableId(table.getId());

        // 新增或更新字段
        for (GenTableColumn dbColumn : dbColumns) {
            GenTableColumn existColumn = tableColumns.stream()
                    .filter(c -> c.getColumnName().equals(dbColumn.getColumnName()))
                    .findFirst()
                    .orElse(null);

            if (existColumn == null) {
                // 新增字段
                GenUtils.initColumnField(dbColumn, table);
                dbColumn.setTableId(table.getId());
                columnMapper.insert(dbColumn);
            } else {
                // 更新字段
                existColumn.setColumnType(dbColumn.getColumnType());
                existColumn.setColumnComment(dbColumn.getColumnComment());
                columnMapper.updateById(existColumn);
            }
        }

        // 删除已不存在的字段
        for (GenTableColumn column : tableColumns) {
            boolean exists = dbColumns.stream()
                    .anyMatch(c -> c.getColumnName().equals(column.getColumnName()));
            if (!exists) {
                columnMapper.deleteById(column.getId());
            }
        }
    }

    /**
     * 生成代码到zip
     */
    private void generateCode(String tableName, ZipOutputStream zip) {
        GenTable table = lambdaQuery().eq(GenTable::getTableName, tableName).one();
        if (table == null) {
            throw new BusinessException("表不存在: " + tableName);
        }
        table = selectGenTableById(table.getId());

        VelocityUtils.initVelocity();
        VelocityContext context = VelocityUtils.prepareContext(table);

        List<String> templates = VelocityUtils.getTemplateList();
        for (String template : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, StandardCharsets.UTF_8.name());
            tpl.merge(context, sw);

            try {
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("生成代码失败", e);
                throw new BusinessException("生成代码失败: " + e.getMessage());
            }
        }
    }
}
