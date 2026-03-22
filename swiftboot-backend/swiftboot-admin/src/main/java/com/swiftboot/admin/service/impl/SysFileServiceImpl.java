package com.swiftboot.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.dto.SysFileQueryDTO;
import com.swiftboot.admin.domain.entity.SysFile;
import com.swiftboot.admin.mapper.SysFileMapper;
import com.swiftboot.admin.service.SysFileService;
import com.swiftboot.admin.service.SysStorageConfigService;
import com.swiftboot.admin.storage.StorageClient;
import com.swiftboot.admin.storage.StorageClientFactory;
import com.swiftboot.admin.storage.StorageObject;
import com.swiftboot.admin.storage.StorageType;
import com.swiftboot.admin.storage.StorageUploadResult;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * File service implementation.
 */
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    private static final String ACCESS_TOKEN_KEY_PREFIX = "file:access:";

    private final StorageClientFactory storageClientFactory;

    private final SysStorageConfigService storageConfigService;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<SysFile> selectFilePage(SysFileQueryDTO query, PageQuery pageQuery) {
        Page<SysFile> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(SysFile::getOriginalName, query.getKeyword())
                    .or()
                    .like(SysFile::getFileName, query.getKeyword()));
        }
        wrapper.eq(StrUtil.isNotBlank(query.getBizType()), SysFile::getBizType, query.getBizType())
                .eq(query.getBizId() != null, SysFile::getBizId, query.getBizId())
                .eq(StrUtil.isNotBlank(query.getStorageType()), SysFile::getStorageType, query.getStorageType())
                .orderByDesc(SysFile::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFile upload(MultipartFile file, String bizType, Long bizId, String visibility) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "Uploaded file is empty");
        }
        var config = storageConfigService.getRuntimeConfig();
        StorageClient client = storageClientFactory.getClient(config.getActiveType());
        String originalName = StrUtil.blankToDefault(file.getOriginalFilename(), "file");
        String fileSuffix = FileUtil.extName(originalName);
        String objectKey = buildObjectKey(bizType, fileSuffix);
        StorageUploadResult uploadResult = null;
        try {
            uploadResult = client.upload(file, objectKey, config);
            SysFile entity = new SysFile();
            entity.setFileName(FileUtil.getName(objectKey));
            entity.setOriginalName(originalName);
            entity.setFileSuffix(fileSuffix);
            entity.setFilePath(uploadResult.getObjectKey());
            entity.setFileSize(file.getSize());
            entity.setStorageType(config.getActiveType());
            entity.setStorageBucket(uploadResult.getBucket());
            entity.setMimeType(StrUtil.blankToDefault(file.getContentType(), FileUtil.getMimeType(originalName)));
            entity.setVisibility(resolveVisibility(config.getActiveType(), config));
            if (StrUtil.isNotBlank(visibility)) {
                entity.setVisibility(visibility);
            }
            entity.setBizType(StrUtil.blankToDefault(bizType, null));
            entity.setBizId(bizId);
            entity.setUrl(uploadResult.getUrl());
            save(entity);
            return entity;
        } catch (Exception e) {
            if (uploadResult != null) {
                try {
                    client.delete(uploadResult.getObjectKey(), uploadResult.getBucket(), config);
                } catch (Exception ignored) {
                }
            }
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "File upload failed: " + e.getMessage());
        }
    }

    @Override
    public String createAccessUrl(Long fileId, String disposition) {
        SysFile file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        var config = storageConfigService.getRuntimeConfig();
        String token = UUID.fastUUID().toString(true);
        JSONObject payload = new JSONObject();
        payload.set("fileId", fileId);
        payload.set("disposition", disposition);
        stringRedisTemplate.opsForValue().set(
                ACCESS_TOKEN_KEY_PREFIX + token,
                payload.toString(),
                config.getAccessUrlExpireSeconds(),
                TimeUnit.SECONDS
        );
        return "/api/system/file/access/" + token;
    }

    @Override
    public SysFile getFileByToken(String token) {
        String payload = stringRedisTemplate.opsForValue().get(ACCESS_TOKEN_KEY_PREFIX + token);
        if (StrUtil.isBlank(payload)) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "Access token expired");
        }
        Long fileId = JSONUtil.parseObj(payload).getLong("fileId");
        SysFile file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return file;
    }

    @Override
    public String getDispositionByToken(String token) {
        String payload = stringRedisTemplate.opsForValue().get(ACCESS_TOKEN_KEY_PREFIX + token);
        if (StrUtil.isBlank(payload)) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "Access token expired");
        }
        return JSONUtil.parseObj(payload).getStr("disposition", "inline");
    }

    @Override
    public StorageObject openStorageObject(SysFile file) throws Exception {
        StorageClient client = storageClientFactory.getClient(file.getStorageType());
        return client.getObject(file.getFilePath(), file.getStorageBucket(), storageConfigService.getRuntimeConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        SysFile file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        try {
            StorageClient client = storageClientFactory.getClient(file.getStorageType());
            client.delete(file.getFilePath(), file.getStorageBucket(), storageConfigService.getRuntimeConfig());
            removeById(fileId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to delete file");
        }
    }

    @Override
    public void renameFile(Long fileId, String newName) {
        SysFile file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        String fileSuffix = StrUtil.blankToDefault(file.getFileSuffix(), "");
        String normalizedName = StrUtil.trim(newName);
        if (StrUtil.isBlank(normalizedName)) {
            throw new BusinessException("File name cannot be empty");
        }
        if (StrUtil.isNotBlank(fileSuffix) && !normalizedName.toLowerCase().endsWith("." + fileSuffix.toLowerCase())) {
            normalizedName = normalizedName + "." + fileSuffix;
        }
        SysFile update = new SysFile();
        update.setId(fileId);
        update.setOriginalName(normalizedName);
        updateById(update);
    }

    private String buildObjectKey(String bizType, String fileSuffix) {
        String folder = StrUtil.blankToDefault(bizType, "common");
        folder = folder.replaceAll("[^a-zA-Z0-9/_-]", "_");
        String dateFolder = LocalDate.now().toString().replace("-", "/");
        String fileName = UUID.fastUUID().toString(true);
        if (StrUtil.isNotBlank(fileSuffix)) {
            fileName = fileName + "." + fileSuffix.toLowerCase();
        }
        return folder + "/" + dateFolder + "/" + fileName;
    }

    private String resolveVisibility(String storageType, com.swiftboot.admin.domain.vo.SysStorageConfigVO config) {
        return switch (storageType) {
            case StorageType.LOCAL -> Boolean.TRUE.equals(config.getLocal().getPublicRead()) ? "public" : "private";
            case StorageType.MINIO -> Boolean.TRUE.equals(config.getMinio().getPublicRead()) ? "public" : "private";
            case StorageType.OSS -> Boolean.TRUE.equals(config.getOss().getPublicRead()) ? "public" : "private";
            case StorageType.COS -> Boolean.TRUE.equals(config.getCos().getPublicRead()) ? "public" : "private";
            default -> "private";
        };
    }
}
