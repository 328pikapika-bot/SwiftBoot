package com.swiftboot.admin.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Upload result returned by storage clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageUploadResult {

    private String objectKey;

    private String bucket;

    private String url;
}
