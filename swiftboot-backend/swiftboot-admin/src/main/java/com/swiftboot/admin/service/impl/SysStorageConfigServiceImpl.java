package com.swiftboot.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.swiftboot.admin.config.StorageProperties;
import com.swiftboot.admin.domain.dto.SysStorageConfigDTO;
import com.swiftboot.admin.domain.vo.SysStorageConfigVO;
import com.swiftboot.admin.service.SysStorageConfigService;
import com.swiftboot.admin.storage.StorageType;
import com.swiftboot.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Storage config service implementation.
 */
@Service
@RequiredArgsConstructor
public class SysStorageConfigServiceImpl implements SysStorageConfigService {

    private static final String STORAGE_CONFIG_KEY = "storage:config";

    private static final Set<String> SUPPORTED_TYPES = Set.of(StorageType.LOCAL, StorageType.MINIO, StorageType.OSS, StorageType.COS);

    private final StringRedisTemplate stringRedisTemplate;

    private final StorageProperties storageProperties;

    @Override
    public SysStorageConfigVO getConfig() {
        String configJson = stringRedisTemplate.opsForValue().get(STORAGE_CONFIG_KEY);
        if (StrUtil.isNotBlank(configJson)) {
            return JSONUtil.toBean(configJson, SysStorageConfigVO.class);
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(storageProperties), SysStorageConfigVO.class);
    }

    @Override
    public SysStorageConfigVO getRuntimeConfig() {
        return getConfig();
    }

    @Override
    public void updateConfig(SysStorageConfigDTO dto) {
        validateConfig(dto);
        stringRedisTemplate.opsForValue().set(STORAGE_CONFIG_KEY, JSONUtil.toJsonStr(dto));
    }

    private void validateConfig(SysStorageConfigDTO dto) {
        if (!SUPPORTED_TYPES.contains(dto.getActiveType())) {
            throw new BusinessException("Unsupported storage type");
        }
        switch (dto.getActiveType()) {
            case StorageType.LOCAL -> {
                if (StrUtil.isBlank(dto.getLocal().getBasePath())) {
                    throw new BusinessException("Local basePath is required");
                }
            }
            case StorageType.MINIO -> {
                if (StrUtil.hasBlank(dto.getMinio().getEndpoint(), dto.getMinio().getAccessKey(), dto.getMinio().getSecretKey(), dto.getMinio().getBucket())) {
                    throw new BusinessException("MinIO endpoint, accessKey, secretKey and bucket are required");
                }
            }
            case StorageType.OSS -> {
                if (StrUtil.hasBlank(dto.getOss().getEndpoint(), dto.getOss().getAccessKeyId(), dto.getOss().getAccessKeySecret(), dto.getOss().getBucket())) {
                    throw new BusinessException("OSS endpoint, accessKeyId, accessKeySecret and bucket are required");
                }
            }
            case StorageType.COS -> {
                if (StrUtil.hasBlank(dto.getCos().getRegion(), dto.getCos().getSecretId(), dto.getCos().getSecretKey(), dto.getCos().getBucket())) {
                    throw new BusinessException("COS region, secretId, secretKey and bucket are required");
                }
            }
            default -> throw new BusinessException("Unsupported storage type");
        }
    }
}
