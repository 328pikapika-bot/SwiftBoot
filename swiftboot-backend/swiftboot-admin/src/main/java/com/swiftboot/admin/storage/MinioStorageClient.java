package com.swiftboot.admin.storage;

import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.common.core.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO storage.
 */
@Component
public class MinioStorageClient extends AbstractStorageClient {

    @Override
    public String getType() {
        return StorageType.MINIO;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String objectKey, SysStorageConfigVO config) throws Exception {
        SysStorageConfigVO.MinioConfig minio = config.getMinio();
        String endpoint = requireText(minio.getEndpoint(), "MinIO endpoint is required");
        String accessKey = requireText(minio.getAccessKey(), "MinIO accessKey is required");
        String secretKey = requireText(minio.getSecretKey(), "MinIO secretKey is required");
        String bucket = requireText(minio.getBucket(), "MinIO bucket is required");
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(detectContentType(file))
                        .build()
        );
        return new StorageUploadResult(objectKey, bucket, buildPublicUrl(minio.getDomain(), objectKey));
    }

    @Override
    public StorageObject getObject(String objectKey, String bucket, SysStorageConfigVO config) throws Exception {
        SysStorageConfigVO.MinioConfig minio = config.getMinio();
        MinioClient client = MinioClient.builder()
                .endpoint(requireText(minio.getEndpoint(), "MinIO endpoint is required"))
                .credentials(
                        requireText(minio.getAccessKey(), "MinIO accessKey is required"),
                        requireText(minio.getSecretKey(), "MinIO secretKey is required")
                )
                .build();
        String targetBucket = requireText(bucket != null ? bucket : minio.getBucket(), "MinIO bucket is required");
        GetObjectResponse response = client.getObject(
                GetObjectArgs.builder()
                        .bucket(targetBucket)
                        .object(objectKey)
                        .build()
        );
        return new StorageObject(response, null, response.headers().get("Content-Type"), response::close);
    }

    @Override
    public void delete(String objectKey, String bucket, SysStorageConfigVO config) throws Exception {
        SysStorageConfigVO.MinioConfig minio = config.getMinio();
        MinioClient client = MinioClient.builder()
                .endpoint(requireText(minio.getEndpoint(), "MinIO endpoint is required"))
                .credentials(
                        requireText(minio.getAccessKey(), "MinIO accessKey is required"),
                        requireText(minio.getSecretKey(), "MinIO secretKey is required")
                )
                .build();
        String targetBucket = requireText(bucket != null ? bucket : minio.getBucket(), "MinIO bucket is required");
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(targetBucket).object(objectKey).build());
        } catch (Exception e) {
            throw new BusinessException("Failed to delete MinIO object");
        }
    }
}
