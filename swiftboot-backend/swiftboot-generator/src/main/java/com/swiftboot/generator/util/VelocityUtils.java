package com.swiftboot.generator.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.swiftboot.generator.domain.GenTable;
import com.swiftboot.generator.domain.GenTableColumn;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Velocity 工具类
 */
public class VelocityUtils {

    /**
     * 初始化 Velocity
     */
    public static void initVelocity() {
        Properties p = new Properties();
        try {
            p.setProperty("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            p.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
            Velocity.init(p);
        } catch (Exception e) {
            throw new RuntimeException("Velocity初始化失败", e);
        }
    }

    /**
     * 准备模板上下文
     */
    public static VelocityContext prepareContext(GenTable table) {
        VelocityContext context = new VelocityContext();

        context.put("tableName", table.getTableName());
        context.put("tableComment", table.getTableComment());
        context.put("className", table.getClassName());
        context.put("classname", StrUtil.lowerFirst(table.getClassName()));
        context.put("packageName", table.getPackageName());
        context.put("moduleName", table.getModuleName());
        context.put("businessName", table.getBusinessName());
        context.put("functionName", table.getFunctionName());
        context.put("author", table.getAuthor());
        context.put("datetime", DateUtil.now());
        context.put("columns", table.getColumns());
        context.put("pkColumn", table.getPkColumn());

        // 生成菜单SQL所需的雪花ID（使用MyBatis-Plus的IdWorker）
        context.put("menuId", IdWorker.getId());
        context.put("menuQueryId", IdWorker.getId());
        context.put("menuAddId", IdWorker.getId());
        context.put("menuEditId", IdWorker.getId());
        context.put("menuRemoveId", IdWorker.getId());

        // 处理导入的包
        List<String> importList = new ArrayList<>();
        for (GenTableColumn column : table.getColumns()) {
            if ("BigDecimal".equals(column.getJavaType()) && !importList.contains("java.math.BigDecimal")) {
                importList.add("java.math.BigDecimal");
            } else if ("LocalDateTime".equals(column.getJavaType()) && !importList.contains("java.time.LocalDateTime")) {
                importList.add("java.time.LocalDateTime");
            } else if ("LocalDate".equals(column.getJavaType()) && !importList.contains("java.time.LocalDate")) {
                importList.add("java.time.LocalDate");
            } else if ("LocalTime".equals(column.getJavaType()) && !importList.contains("java.time.LocalTime")) {
                importList.add("java.time.LocalTime");
            }
        }
        context.put("importList", importList);

        return context;
    }

    /**
     * 获取模板列表
     */
    public static List<String> getTemplateList() {
        List<String> templates = new ArrayList<>();
        templates.add("vm/java/entity.java.vm");
        templates.add("vm/java/mapper.java.vm");
        templates.add("vm/java/service.java.vm");
        templates.add("vm/java/serviceImpl.java.vm");
        templates.add("vm/java/controller.java.vm");
        templates.add("vm/xml/mapper.xml.vm");
        templates.add("vm/vue/index.vue.vm");
        templates.add("vm/vue/api.ts.vm");
        templates.add("vm/vue/router.ts.vm");
        templates.add("vm/sql/sql.vm");
        return templates;
    }

    /**
     * 获取生成文件名
     */
    public static String getFileName(String template, GenTable table) {
        String className = table.getClassName();
        String businessName = table.getBusinessName();
        String packageName = table.getPackageName();
        String javaPath = packageName.replace(".", "/");

        if (template.contains("entity.java.vm")) {
            return javaPath + "/domain/entity/" + className + ".java";
        } else if (template.contains("mapper.java.vm")) {
            return javaPath + "/mapper/" + className + "Mapper.java";
        } else if (template.contains("service.java.vm")) {
            return javaPath + "/service/" + className + "Service.java";
        } else if (template.contains("serviceImpl.java.vm")) {
            return javaPath + "/service/impl/" + className + "ServiceImpl.java";
        } else if (template.contains("controller.java.vm")) {
            return javaPath + "/controller/" + className + "Controller.java";
        } else if (template.contains("mapper.xml.vm")) {
            return "mapper/" + className + "Mapper.xml";
        } else if (template.contains("index.vue.vm")) {
            return "vue/" + businessName + "/index.vue";
        } else if (template.contains("api.ts.vm")) {
            return "vue/api/" + businessName + ".ts";
        } else if (template.contains("router.ts.vm")) {
            return "vue/router/" + businessName + "-routes.ts";
        } else if (template.contains("sql.vm")) {
            return "sql/" + businessName + "_menu.sql";
        }
        return "";
    }
}
