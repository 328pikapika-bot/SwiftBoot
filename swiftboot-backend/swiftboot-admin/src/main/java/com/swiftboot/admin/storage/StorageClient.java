package com.swiftboot.admin.storage;

import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage client abstraction.
 */
public interface StorageClient {

    String getType();

    StorageUploadResult upload(MultipartFile file, String objectKey, SysStorageConfigVO config) throws Exception;

    StorageObject getObject(String objectKey, String bucket, SysStorageConfigVO config) throws Exception;

    void delete(String objectKey, String bucket, SysStorageConfigVO config) throws Exception;
}
