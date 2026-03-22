package com.swiftboot.admin.storage;

import cn.hutool.core.util.StrUtil;
import com.swiftboot.common.core.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;

/**
 * Common helpers for storage clients.
 */
public abstract class AbstractStorageClient implements StorageClient {

    protected String requireText(String value, String message) {
        if (StrUtil.isBlank(value)) {
            throw new BusinessException(message);
        }
        return value;
    }

    protected String detectContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (StrUtil.isNotBlank(contentType)) {
            return contentType;
        }
        return URLConnection.guessContentTypeFromName(file.getOriginalFilename());
    }

    protected String buildPublicUrl(String domain, String objectKey) {
        if (StrUtil.isBlank(domain)) {
            return null;
        }
        return StrUtil.removeSuffix(domain.trim(), "/") + "/" + StrUtil.removePrefix(objectKey, "/");
    }
}
