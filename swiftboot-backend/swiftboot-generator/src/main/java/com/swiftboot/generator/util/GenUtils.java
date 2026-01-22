package com.swiftboot.generator.util;

import cn.hutool.core.util.StrUtil;
import com.swiftboot.generator.domain.GenTable;
import com.swiftboot.generator.domain.GenTableColumn;

import java.util.Arrays;

/**
 * 代码生成工具类
 */
public class GenUtils {

    /**
     * 初始化表信息
     */
    public static void initTable(GenTable table) {
        table.setClassName(convertClassName(table.getTableName()));
        table.setPackageName("com.swiftboot.admin");
        table.setModuleName("admin");
        table.setBusinessName(getBusinessName(table.getTableName()));
        table.setFunctionName(table.getTableComment());
        table.setAuthor("SwiftBoot_chenshuang");
        table.setGenType("0");
    }

    /**
     * 初始化字段信息
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        String columnName = column.getColumnName();
        String columnType = column.getColumnType();

        column.setJavaField(toCamelCase(columnName));
        column.setJavaType(getJavaType(columnType));

        // 插入字段
        if (!arraysContains(new String[]{"id", "create_by", "create_time", "update_by", "update_time", "deleted"}, columnName)) {
            column.setIsInsert("1");
        }

        // 编辑字段
        if (!arraysContains(new String[]{"id", "create_by", "create_time", "update_by", "update_time", "deleted"}, columnName)) {
            column.setIsEdit("1");
        }

        // 列表字段
        if (!arraysContains(new String[]{"id", "create_by", "update_by", "deleted", "remark"}, columnName)) {
            column.setIsList("1");
        }

        // 查询字段
        if (!arraysContains(new String[]{"id", "create_by", "create_time", "update_by", "update_time", "deleted", "remark"}, columnName)) {
            column.setIsQuery("1");
        }

        // 查询类型
        if (StrUtil.endWith(columnName, "name") || StrUtil.endWith(columnName, "title")) {
            column.setQueryType("LIKE");
        } else {
            column.setQueryType("EQ");
        }

        // 显示类型
        if (StrUtil.endWith(columnName, "status") || StrUtil.endWith(columnName, "type") || StrUtil.endWith(columnName, "gender")) {
            column.setHtmlType("select");
        } else if (StrUtil.endWith(columnName, "time") || StrUtil.endWith(columnName, "date")) {
            column.setHtmlType("datetime");
        } else if (StrUtil.endWith(columnName, "content") || StrUtil.endWith(columnName, "remark")) {
            column.setHtmlType("textarea");
        } else if (StrUtil.endWith(columnName, "image") || StrUtil.endWith(columnName, "avatar")) {
            column.setHtmlType("imageUpload");
        } else if (StrUtil.endWith(columnName, "file")) {
            column.setHtmlType("fileUpload");
        } else {
            column.setHtmlType("input");
        }
    }

    /**
     * 表名转类名
     */
    public static String convertClassName(String tableName) {
        // 去除表前缀
        String[] prefixes = {"sys_", "gen_"};
        for (String prefix : prefixes) {
            if (tableName.startsWith(prefix)) {
                tableName = tableName.substring(prefix.length());
                break;
            }
        }
        return toPascalCase(tableName);
    }

    /**
     * 获取业务名
     */
    public static String getBusinessName(String tableName) {
        // 去除表前缀
        String[] prefixes = {"sys_", "gen_"};
        for (String prefix : prefixes) {
            if (tableName.startsWith(prefix)) {
                tableName = tableName.substring(prefix.length());
                break;
            }
        }
        return toCamelCase(tableName);
    }

    /**
     * 获取Java类型
     */
    public static String getJavaType(String columnType) {
        if (columnType.contains("bigint")) {
            return "Long";
        } else if (columnType.contains("int")) {
            return "Integer";
        } else if (columnType.contains("decimal") || columnType.contains("double") || columnType.contains("float")) {
            return "BigDecimal";
        } else if (columnType.contains("datetime") || columnType.contains("timestamp")) {
            return "LocalDateTime";
        } else if (columnType.contains("date")) {
            return "LocalDate";
        } else if (columnType.contains("time")) {
            return "LocalTime";
        } else {
            return "String";
        }
    }

    /**
     * 下划线转驼峰
     */
    public static String toCamelCase(String str) {
        if (StrUtil.isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 下划线转帕斯卡
     */
    public static String toPascalCase(String str) {
        String camelCase = toCamelCase(str);
        if (StrUtil.isNotBlank(camelCase)) {
            return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
        }
        return camelCase;
    }

    /**
     * 检查数组是否包含元素
     */
    public static boolean arraysContains(String[] arr, String str) {
        return Arrays.asList(arr).contains(str);
    }
}
