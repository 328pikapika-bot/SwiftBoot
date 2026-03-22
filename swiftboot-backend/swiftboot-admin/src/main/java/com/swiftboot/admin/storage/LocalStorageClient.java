package com.swiftboot.admin.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local disk storage.
 */
@Slf4j
@Component
public class LocalStorageClient extends AbstractStorageClient {

    @Override
    public String getType() {
        return StorageType.LOCAL;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String objectKey, SysStorageConfigVO config) throws Exception {
        String basePath = requireText(config.getLocal().getBasePath(), "Local storage basePath is required");
        Path target = Path.of(basePath).resolve(objectKey).normalize();
        Files.createDirectories(target.getParent());
        file.transferTo(target);
        return new StorageUploadResult(objectKey, null, null);
    }

    @Override
    public StorageObject getObject(String objectKey, String bucket, SysStorageConfigVO config) throws Exception {
        String basePath = requireText(config.getLocal().getBasePath(), "Local storage basePath is required");
        Path target = Path.of(basePath).resolve(objectKey).normalize();
        if (!Files.exists(target)) {
            throw new BusinessException("File does not exist in local storage");
        }
        String contentType = Files.probeContentType(target);
        return new StorageObject(
                Files.newInputStream(target),
                Files.size(target),
                StrUtil.blankToDefault(contentType, FileUtil.getMimeType(target.toString())),
                null
        );
    }

    @Override
    public void delete(String objectKey, String bucket, SysStorageConfigVO config) {
        String basePath = requireText(config.getLocal().getBasePath(), "Local storage basePath is required");
        Path target = Path.of(basePath).resolve(objectKey).normalize();
        try {
            Files.deleteIfExists(target);
        } catch (Exception e) {
            log.warn("Failed to delete local file: {}", target, e);
            throw new BusinessException("Failed to delete local file");
        }
    }
}
