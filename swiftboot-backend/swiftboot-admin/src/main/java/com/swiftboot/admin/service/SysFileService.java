package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.dto.SysFileQueryDTO;
import com.swiftboot.admin.domain.entity.SysFile;
import com.swiftboot.admin.storage.StorageObject;
import com.swiftboot.common.core.domain.PageQuery;
import org.springframework.web.multipart.MultipartFile;

/**
 * File service.
 */
public interface SysFileService extends IService<SysFile> {

    Page<SysFile> selectFilePage(SysFileQueryDTO query, PageQuery pageQuery);

    SysFile upload(MultipartFile file, String bizType, Long bizId, String visibility);

    String createAccessUrl(Long fileId, String disposition);

    SysFile getFileByToken(String token);

    String getDispositionByToken(String token);

    StorageObject openStorageObject(SysFile file) throws Exception;

    void deleteFile(Long fileId);

    void renameFile(Long fileId, String newName);
}
