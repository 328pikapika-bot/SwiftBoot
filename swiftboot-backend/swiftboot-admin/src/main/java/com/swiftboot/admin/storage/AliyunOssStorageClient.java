package com.swiftboot.admin.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Aliyun OSS storage.
 */
@Component
public class AliyunOssStorageClient extends AbstractStorageClient {

    @Override
    public String getType() {
        return StorageType.OSS;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String objectKey, SysStorageConfigVO config) throws Exception {
        SysStorageConfigVO.OssConfig ossConfig = config.getOss();
        String endpoint = requireText(ossConfig.getEndpoint(), "OSS endpoint is required");
        String accessKeyId = requireText(ossConfig.getAccessKeyId(), "OSS accessKeyId is required");
        String accessKeySecret = requireText(ossConfig.getAccessKeySecret(), "OSS accessKeySecret is required");
        String bucket = requireText(ossConfig.getBucket(), "OSS bucket is required");
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(detectContentType(file));
            client.putObject(bucket, objectKey, file.getInputStream(), metadata);
            return new StorageUploadResult(objectKey, bucket, buildPublicUrl(ossConfig.getDomain(), objectKey));
        } finally {
            client.shutdown();
        }
    }

    @Override
    public StorageObject getObject(String objectKey, String bucket, SysStorageConfigVO config) {
        SysStorageConfigVO.OssConfig ossConfig = config.getOss();
        OSS client = new OSSClientBuilder().build(
                requireText(ossConfig.getEndpoint(), "OSS endpoint is required"),
                requireText(ossConfig.getAccessKeyId(), "OSS accessKeyId is required"),
                requireText(ossConfig.getAccessKeySecret(), "OSS accessKeySecret is required")
        );
        String targetBucket = requireText(bucket != null ? bucket : ossConfig.getBucket(), "OSS bucket is required");
        OSSObject object = client.getObject(targetBucket, objectKey);
        if (object == null) {
            client.shutdown();
            throw new BusinessException("OSS object does not exist");
        }
        return new StorageObject(
                object.getObjectContent(),
                object.getObjectMetadata() == null ? null : object.getObjectMetadata().getContentLength(),
                object.getObjectMetadata() == null ? null : object.getObjectMetadata().getContentType(),
                () -> {
                    object.close();
                    client.shutdown();
                }
        );
    }

    @Override
    public void delete(String objectKey, String bucket, SysStorageConfigVO config) {
        SysStorageConfigVO.OssConfig ossConfig = config.getOss();
        OSS client = new OSSClientBuilder().build(
                requireText(ossConfig.getEndpoint(), "OSS endpoint is required"),
                requireText(ossConfig.getAccessKeyId(), "OSS accessKeyId is required"),
                requireText(ossConfig.getAccessKeySecret(), "OSS accessKeySecret is required")
        );
        try {
            client.deleteObject(requireText(bucket != null ? bucket : ossConfig.getBucket(), "OSS bucket is required"), objectKey);
        } catch (Exception e) {
            throw new BusinessException("Failed to delete OSS object");
        } finally {
            client.shutdown();
        }
    }
}
