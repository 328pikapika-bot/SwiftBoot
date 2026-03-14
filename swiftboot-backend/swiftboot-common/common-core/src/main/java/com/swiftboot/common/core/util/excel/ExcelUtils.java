package com.swiftboot.common.core.util.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel工具类
 */
@Slf4j
public class ExcelUtils {

    /**
     * 导出Excel
     *
     * @param response  HttpServletResponse
     * @param data      数据List
     * @param sheetName Sheet名称
     * @param clazz     表头Class
     */
    public static void exportExcel(HttpServletResponse response, List<?> data, String sheetName, Class<?> clazz) {
        try {
            setExportResponse(response, sheetName);
            OutputStream outputStream = response.getOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            excelWriter.write(data, writeSheet);
            excelWriter.finish();
        } catch (IOException e) {
            log.error("导出Excel失败: {}", e.getMessage());
        }
    }

    /**
     * 导入Excel
     *
     * @param inputStream 输入流
     * @param clazz       表头Class
     * @return List
     */
    public static List<?> importExcel(InputStream inputStream, Class<?> clazz) {
        List<Object> list = new ArrayList<>();
        ExcelReader excelReader = EasyExcel.read(inputStream, clazz, new ReadListener<Object>() {
            @Override
            public void invoke(Object o, AnalysisContext analysisContext) {
                list.add(o);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                // 读取完成
            }
        }).build();
        excelReader.read();
        excelReader.finish();
        return list;
    }

    /**
     * 设置响应
     */
    private static void setExportResponse(HttpServletResponse response, String fileName) {
        try {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        } catch (Exception e) {
            log.error("设置响应失败: {}", e.getMessage());
        }
    }
}
