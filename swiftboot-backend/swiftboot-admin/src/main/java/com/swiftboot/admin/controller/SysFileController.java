package com.swiftboot.admin.controller;

import cn.hutool.core.io.FileUtil;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传 Controller
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/file")
public class SysFileController {

    @Value("${swiftboot.file.uploadPath:./upload}")
    private String uploadPath;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("文件不能为空");
        }

        try {
            String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File uploadDir = new File(uploadPath, datePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保持原始文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = originalFilename;
            
            // 如果文件已存在，加时间戳避免冲突
            File targetFile = new File(uploadDir, fileName);
            if (targetFile.exists()) {
                String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                fileName = nameWithoutExt + "_" + System.currentTimeMillis() + ext;
            }
            
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("originalName", originalFilename);
            result.put("filePath", "/upload/" + datePath + "/" + fileName);
            result.put("fileSize", file.getSize());
            result.put("createTime", new Date());

            return R.ok(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文件列表")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        List<Map<String, Object>> fileList = new ArrayList<>();
        try {
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                File[] dateDirs = uploadDir.listFiles();
                if (dateDirs != null) {
                    for (File dateDir : dateDirs) {
                        if (dateDir.isDirectory()) {
                            File[] files = dateDir.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    Map<String, Object> fileInfo = new HashMap<>();
                                    fileInfo.put("fileName", file.getName());
                                    fileInfo.put("originalName", file.getName());
                                    fileInfo.put("filePath", "/upload/" + dateDir.getName() + "/" + file.getName());
                                    fileInfo.put("fileSize", file.length());
                                    fileInfo.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified())));
                                    fileList.add(fileInfo);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
        }
        return R.ok(fileList);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileName}")
    public R<Void> delete(@PathVariable String fileName) {
        try {
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists()) {
                File[] dirs = uploadDir.listFiles();
                if (dirs != null) {
                    for (File dir : dirs) {
                        File target = new File(dir, fileName);
                        if (target.exists() && target.delete()) {
                            return R.ok();
                        }
                    }
                }
            }
            return R.fail("文件不存在");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return R.fail("文件删除失败");
        }
    }

    @Operation(summary = "预览文件")
    @GetMapping("/preview/{fileName}")
    public void preview(@PathVariable String fileName, HttpServletResponse response) {
        try {
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists()) {
                File[] dirs = uploadDir.listFiles();
                if (dirs != null) {
                    for (File dir : dirs) {
                        File target = new File(dir, fileName);
                        if (target.exists()) {
                            String ext = FileUtil.extName(fileName).toLowerCase();
                            String contentType = getContentType(ext);
                            response.setContentType(contentType);
                            response.setHeader("Content-Disposition", "inline; filename=" + fileName);
                            Files.copy(target.toPath(), response.getOutputStream());
                            return;
                        }
                    }
                }
            }
            response.setStatus(404);
        } catch (Exception e) {
            log.error("文件预览失败", e);
            response.setStatus(500);
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists()) {
                File[] dirs = uploadDir.listFiles();
                if (dirs != null) {
                    for (File dir : dirs) {
                        File target = new File(dir, fileName);
                        if (target.exists()) {
                            String ext = FileUtil.extName(fileName).toLowerCase();
                            String contentType = getContentType(ext);
                            response.setContentType(contentType);
                            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                            Files.copy(target.toPath(), response.getOutputStream());
                            return;
                        }
                    }
                }
            }
            response.setStatus(404);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            response.setStatus(500);
        }
    }

    private String getContentType(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp", "bmp" -> "image/" + ext;
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "txt" -> "text/plain";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    @Operation(summary = "重命名文件")
    @PostMapping("/rename")
    public R<Void> rename(@RequestBody Map<String, String> params) {
        try {
            String oldName = params.get("oldName");
            String newName = params.get("newName");
            
            if (oldName == null || newName == null || oldName.isEmpty() || newName.isEmpty()) {
                return R.fail("文件名不能为空");
            }
            
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists()) {
                File[] dirs = uploadDir.listFiles();
                if (dirs != null) {
                    for (File dir : dirs) {
                        File oldFile = new File(dir, oldName);
                        if (oldFile.exists()) {
                            // 获取扩展名
                            String ext = "";
                            if (oldName.contains(".")) {
                                ext = oldName.substring(oldName.lastIndexOf("."));
                            }
                            // 新文件名添加扩展名
                            if (!newName.contains(".")) {
                                newName = newName + ext;
                            }
                            File newFile = new File(dir, newName);
                            if (oldFile.renameTo(newFile)) {
                                return R.ok();
                            }
                            return R.fail("重命名失败");
                        }
                    }
                }
            }
            return R.fail("文件不存在");
        } catch (Exception e) {
            log.error("文件重命名失败", e);
            return R.fail("重命名失败");
        }
    }
}
