package com.swiftboot.admin.controller;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swiftboot.admin.domain.dto.SysFileQueryDTO;
import com.swiftboot.admin.domain.dto.SysFileRenameDTO;
import com.swiftboot.admin.domain.entity.SysFile;
import com.swiftboot.admin.service.SysFileService;
import com.swiftboot.admin.storage.StorageObject;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.PageResult;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * File management controller.
 */
@Slf4j
@Tag(name = "File management")
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class SysFileController {

    private final SysFileService fileService;

    @Operation(summary = "List files")
    @GetMapping("/list")
    public R<PageResult<SysFile>> list(SysFileQueryDTO query, PageQuery pageQuery) {
        Page<SysFile> page = fileService.selectFilePage(query, pageQuery);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "Upload file")
    @Log(title = "File management", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public R<SysFile> upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String bizType,
                             @RequestParam(required = false) Long bizId,
                             @RequestParam(required = false) String visibility) {
        return R.ok(fileService.upload(file, bizType, bizId, visibility));
    }

    @Operation(summary = "Delete file")
    @Log(title = "File management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileId}")
    public R<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return R.ok();
    }

    @Operation(summary = "Rename file")
    @Log(title = "File management", businessType = BusinessType.UPDATE)
    @PutMapping("/{fileId}/rename")
    public R<Void> rename(@PathVariable Long fileId, @Valid @RequestBody SysFileRenameDTO dto) {
        fileService.renameFile(fileId, dto.getNewName());
        return R.ok();
    }

    @Operation(summary = "Get preview URL")
    @GetMapping("/{fileId}/preview-url")
    public R<String> previewUrl(@PathVariable Long fileId) {
        return R.ok(fileService.createAccessUrl(fileId, "inline"));
    }

    @Operation(summary = "Get download URL")
    @GetMapping("/{fileId}/download-url")
    public R<String> downloadUrl(@PathVariable Long fileId) {
        return R.ok(fileService.createAccessUrl(fileId, "attachment"));
    }

    @Operation(summary = "Access file content")
    @GetMapping("/access/{token}")
    public void access(@PathVariable String token, HttpServletResponse response) {
        try {
            SysFile file = fileService.getFileByToken(token);
            String disposition = fileService.getDispositionByToken(token);
            try (StorageObject object = fileService.openStorageObject(file)) {
                String contentType = object.getContentType() != null ? object.getContentType() : file.getMimeType();
                response.setContentType(contentType != null ? contentType : "application/octet-stream");
                if (object.getContentLength() != null && object.getContentLength() >= 0) {
                    response.setContentLengthLong(object.getContentLength());
                }
                String encodedName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
                response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''" + encodedName);
                IoUtil.copy(object.getInputStream(), response.getOutputStream());
                response.flushBuffer();
            }
        } catch (BusinessException e) {
            log.warn("File access rejected: {}", e.getMessage());
            response.setStatus(404);
        } catch (Exception e) {
            log.error("File access failed", e);
            response.setStatus(500);
        }
    }
}
