package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Storage configuration DTO.
 */
@Data
@Schema(description = "Storage configuration")
public class SysStorageConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "activeType is required")
    @Schema(description = "Current active storage type")
    private String activeType = "local";

    @Min(value = 60, message = "accessUrlExpireSeconds must be at least 60")
    @Max(value = 86400, message = "accessUrlExpireSeconds must be at most 86400")
    @Schema(description = "Temporary access URL expiration in seconds")
    private Integer accessUrlExpireSeconds = 900;

    @Valid
    @Schema(description = "Local storage config")
    private LocalConfig local = new LocalConfig();

    @Valid
    @Schema(description = "MinIO storage config")
    private MinioConfig minio = new MinioConfig();

    @Valid
    @Schema(description = "Aliyun OSS storage config")
    private OssConfig oss = new OssConfig();

    @Valid
    @Schema(description = "Tencent COS storage config")
    private CosConfig cos = new CosConfig();

    @Data
    public static class LocalConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Local storage base path")
        private String basePath = "./upload";

        @Schema(description = "Whether uploaded files are public")
        private Boolean publicRead = false;
    }

    @Data
    public static class MinioConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "MinIO endpoint")
        private String endpoint;

        @Schema(description = "MinIO access key")
        private String accessKey;

        @Schema(description = "MinIO secret key")
        private String secretKey;

        @Schema(description = "MinIO bucket")
        private String bucket;

        @Schema(description = "Optional external domain")
        private String domain;

        @Schema(description = "Whether uploaded files are public")
        private Boolean publicRead = false;
    }

    @Data
    public static class OssConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "OSS endpoint")
        private String endpoint;

        @Schema(description = "OSS access key id")
        private String accessKeyId;

        @Schema(description = "OSS access key secret")
        private String accessKeySecret;

        @Schema(description = "OSS bucket")
        private String bucket;

        @Schema(description = "Optional external domain")
        private String domain;

        @Schema(description = "Whether uploaded files are public")
        private Boolean publicRead = false;
    }

    @Data
    public static class CosConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "COS region")
        private String region;

        @Schema(description = "COS secret id")
        private String secretId;

        @Schema(description = "COS secret key")
        private String secretKey;

        @Schema(description = "COS bucket")
        private String bucket;

        @Schema(description = "Optional external domain")
        private String domain;

        @Schema(description = "Whether uploaded files are public")
        private Boolean publicRead = false;
    }
}
