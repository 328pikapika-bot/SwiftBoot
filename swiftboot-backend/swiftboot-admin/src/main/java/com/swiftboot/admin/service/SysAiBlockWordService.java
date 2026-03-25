package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.dto.SysAiBlockCategoryDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordBatchDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordDTO;
import com.swiftboot.admin.domain.vo.SysAiBlockOverviewVO;

/**
 * AI block word service.
 */
public interface SysAiBlockWordService {

    SysAiBlockOverviewVO getOverview();

    void saveCategory(SysAiBlockCategoryDTO dto);

    void deleteCategory(Long categoryId);

    void saveWord(SysAiBlockWordDTO dto);

    void batchSaveWords(SysAiBlockWordBatchDTO dto);

    void deleteWord(Long wordId);

    String checkBlocked(String content);
}
