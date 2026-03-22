package com.swiftboot.admin.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tencent COS storage.
 */
@Component
public class TencentCosStorageClient extends AbstractStorageClient {

    @Override
    public String getType() {
        return StorageType.COS;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String objectKey, SysStorageConfigVO config) throws Exception {
        SysStorageConfigVO.CosConfig cos = config.getCos();
        COSClient client = buildClient(cos);
        try {
            String bucket = requireText(cos.getBucket(), "COS bucket is required");
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(detectContentType(file));
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file.getInputStream(), metadata);
            client.putObject(request);
            return new StorageUploadResult(objectKey, bucket, buildPublicUrl(cos.getDomain(), objectKey));
        } finally {
            client.shutdown();
        }
    }

    @Override
    public StorageObject getObject(String objectKey, String bucket, SysStorageConfigVO config) {
        SysStorageConfigVO.CosConfig cos = config.getCos();
        COSClient client = buildClient(cos);
        String targetBucket = requireText(bucket != null ? bucket : cos.getBucket(), "COS bucket is required");
        COSObject object = client.getObject(targetBucket, objectKey);
        if (object == null) {
            client.shutdown();
            throw new BusinessException("COS object does not exist");
        }
        COSObjectInputStream stream = object.getObjectContent();
        ObjectMetadata metadata = object.getObjectMetadata();
        return new StorageObject(
                stream,
                metadata == null ? null : metadata.getContentLength(),
                metadata == null ? null : metadata.getContentType(),
                () -> {
                    stream.close();
                    object.close();
                    client.shutdown();
                }
        );
    }

    @Override
    public void delete(String objectKey, String bucket, SysStorageConfigVO config) {
        SysStorageConfigVO.CosConfig cos = config.getCos();
        COSClient client = buildClient(cos);
        try {
            client.deleteObject(requireText(bucket != null ? bucket : cos.getBucket(), "COS bucket is required"), objectKey);
        } catch (Exception e) {
            throw new BusinessException("Failed to delete COS object");
        } finally {
            client.shutdown();
        }
    }

    private COSClient buildClient(SysStorageConfigVO.CosConfig cos) {
        COSCredentials credentials = new BasicCOSCredentials(
                requireText(cos.getSecretId(), "COS secretId is required"),
                requireText(cos.getSecretKey(), "COS secretKey is required")
        );
        ClientConfig clientConfig = new ClientConfig(new Region(requireText(cos.getRegion(), "COS region is required")));
        return new COSClient(credentials, clientConfig);
    }
}
