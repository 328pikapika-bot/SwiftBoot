package com.swiftboot.admin.storage;

/**
 * Supported storage types.
 */
public interface StorageType {
    String LOCAL = "local";
    String MINIO = "minio";
    String OSS = "oss";
    String COS = "cos";
}
