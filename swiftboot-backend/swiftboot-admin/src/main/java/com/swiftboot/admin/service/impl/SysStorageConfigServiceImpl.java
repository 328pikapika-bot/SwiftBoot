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
    private static final String MASK = "****";

    private static final Set<String> SUPPORTED_TYPES = Set.of(StorageType.LOCAL, StorageType.MINIO, StorageType.OSS, StorageType.COS);

    private final StringRedisTemplate stringRedisTemplate;

    private final StorageProperties storageProperties;

    @Override
    public SysStorageConfigVO getConfig() {
        return maskSensitiveFields(loadConfig());
    }

    @Override
    public SysStorageConfigVO getRuntimeConfig() {
        return loadConfig();
    }

    @Override
    public void updateConfig(SysStorageConfigDTO dto) {
        SysStorageConfigVO currentConfig = loadConfig();
        SysStorageConfigDTO mergedConfig = mergeSensitiveFields(dto, currentConfig);
        validateConfig(mergedConfig);
        stringRedisTemplate.opsForValue().set(STORAGE_CONFIG_KEY, JSONUtil.toJsonStr(mergedConfig));
    }

    private SysStorageConfigVO loadConfig() {
        String configJson = stringRedisTemplate.opsForValue().get(STORAGE_CONFIG_KEY);
        if (StrUtil.isNotBlank(configJson)) {
            return normalizeConfig(JSONUtil.toBean(configJson, SysStorageConfigVO.class));
        }
        return normalizeConfig(JSONUtil.toBean(JSONUtil.toJsonStr(storageProperties), SysStorageConfigVO.class));
    }

    private void validateConfig(SysStorageConfigDTO dto) {
        normalizeConfig(dto);
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

    private SysStorageConfigVO maskSensitiveFields(SysStorageConfigVO config) {
        SysStorageConfigVO masked = copyConfig(config, SysStorageConfigVO.class);
        masked.getMinio().setAccessKey(maskSecret(masked.getMinio().getAccessKey()));
        masked.getMinio().setSecretKey(maskSecret(masked.getMinio().getSecretKey()));
        masked.getOss().setAccessKeyId(maskSecret(masked.getOss().getAccessKeyId()));
        masked.getOss().setAccessKeySecret(maskSecret(masked.getOss().getAccessKeySecret()));
        masked.getCos().setSecretId(maskSecret(masked.getCos().getSecretId()));
        masked.getCos().setSecretKey(maskSecret(masked.getCos().getSecretKey()));
        return masked;
    }

    private SysStorageConfigDTO mergeSensitiveFields(SysStorageConfigDTO incoming, SysStorageConfigVO current) {
        SysStorageConfigDTO merged = copyConfig(normalizeConfig(incoming), SysStorageConfigDTO.class);
        SysStorageConfigVO normalizedCurrent = normalizeConfig(current);

        if (shouldKeepCurrentSecret(merged.getMinio().getAccessKey())) {
            merged.getMinio().setAccessKey(normalizedCurrent.getMinio().getAccessKey());
        }
        if (shouldKeepCurrentSecret(merged.getMinio().getSecretKey())) {
            merged.getMinio().setSecretKey(normalizedCurrent.getMinio().getSecretKey());
        }
        if (shouldKeepCurrentSecret(merged.getOss().getAccessKeyId())) {
            merged.getOss().setAccessKeyId(normalizedCurrent.getOss().getAccessKeyId());
        }
        if (shouldKeepCurrentSecret(merged.getOss().getAccessKeySecret())) {
            merged.getOss().setAccessKeySecret(normalizedCurrent.getOss().getAccessKeySecret());
        }
        if (shouldKeepCurrentSecret(merged.getCos().getSecretId())) {
            merged.getCos().setSecretId(normalizedCurrent.getCos().getSecretId());
        }
        if (shouldKeepCurrentSecret(merged.getCos().getSecretKey())) {
            merged.getCos().setSecretKey(normalizedCurrent.getCos().getSecretKey());
        }
        return merged;
    }

    private String maskSecret(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        if (value.length() <= 8) {
            return MASK;
        }
        return value.substring(0, 4) + MASK + value.substring(value.length() - 4);
    }

    private boolean shouldKeepCurrentSecret(String value) {
        return StrUtil.isBlank(value) || value.contains(MASK);
    }

    private <T extends SysStorageConfigDTO> T copyConfig(SysStorageConfigDTO source, Class<T> type) {
        return JSONUtil.toBean(JSONUtil.toJsonStr(source), type);
    }

    private <T extends SysStorageConfigDTO> T normalizeConfig(T config) {
        if (config.getLocal() == null) {
            config.setLocal(new SysStorageConfigDTO.LocalConfig());
        }
        if (config.getMinio() == null) {
            config.setMinio(new SysStorageConfigDTO.MinioConfig());
        }
        if (config.getOss() == null) {
            config.setOss(new SysStorageConfigDTO.OssConfig());
        }
        if (config.getCos() == null) {
            config.setCos(new SysStorageConfigDTO.CosConfig());
        }
        return config;
    }
}
