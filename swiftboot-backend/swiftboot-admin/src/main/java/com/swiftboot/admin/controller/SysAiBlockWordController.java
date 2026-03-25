package com.swiftboot.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.swiftboot.admin.domain.dto.SysAiBlockCategoryDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordBatchDTO;
import com.swiftboot.admin.domain.dto.SysAiBlockWordDTO;
import com.swiftboot.admin.domain.vo.SysAiBlockOverviewVO;
import com.swiftboot.admin.service.SysAiBlockWordService;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.log.annotation.Log;
import com.swiftboot.common.log.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI block word management controller.
 */
@Tag(name = "AI block word management")
@RestController
@RequestMapping("/system/ai/block-words")
@RequiredArgsConstructor
public class SysAiBlockWordController {

    private final SysAiBlockWordService aiBlockWordService;

    @Operation(summary = "Get block word overview")
    @GetMapping("/overview")
    @SaCheckPermission("tool:config:list")
    public R<SysAiBlockOverviewVO> getOverview() {
        return R.ok(aiBlockWordService.getOverview());
    }

    @Operation(summary = "Create block category")
    @PostMapping("/categories")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词分类", businessType = BusinessType.INSERT)
    public R<Void> createCategory(@Valid @RequestBody SysAiBlockCategoryDTO dto) {
        aiBlockWordService.saveCategory(dto);
        return R.ok();
    }

    @Operation(summary = "Update block category")
    @PutMapping("/categories")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词分类", businessType = BusinessType.UPDATE)
    public R<Void> updateCategory(@Valid @RequestBody SysAiBlockCategoryDTO dto) {
        aiBlockWordService.saveCategory(dto);
        return R.ok();
    }

    @Operation(summary = "Delete block category")
    @DeleteMapping("/categories/{categoryId}")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词分类", businessType = BusinessType.DELETE)
    public R<Void> deleteCategory(@PathVariable Long categoryId) {
        aiBlockWordService.deleteCategory(categoryId);
        return R.ok();
    }

    @Operation(summary = "Create block word")
    @PostMapping("/words")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词", businessType = BusinessType.INSERT)
    public R<Void> createWord(@Valid @RequestBody SysAiBlockWordDTO dto) {
        aiBlockWordService.saveWord(dto);
        return R.ok();
    }

    @Operation(summary = "Update block word")
    @PutMapping("/words")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词", businessType = BusinessType.UPDATE)
    public R<Void> updateWord(@Valid @RequestBody SysAiBlockWordDTO dto) {
        aiBlockWordService.saveWord(dto);
        return R.ok();
    }

    @Operation(summary = "Batch create block words")
    @PostMapping("/words/batch")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词", businessType = BusinessType.INSERT)
    public R<Void> batchCreateWords(@Valid @RequestBody SysAiBlockWordBatchDTO dto) {
        aiBlockWordService.batchSaveWords(dto);
        return R.ok();
    }

    @Operation(summary = "Delete block word")
    @DeleteMapping("/words/{wordId}")
    @SaCheckPermission("tool:config:edit")
    @Log(title = "AI屏蔽词", businessType = BusinessType.DELETE)
    public R<Void> deleteWord(@PathVariable Long wordId) {
        aiBlockWordService.deleteWord(wordId);
        return R.ok();
    }
}
