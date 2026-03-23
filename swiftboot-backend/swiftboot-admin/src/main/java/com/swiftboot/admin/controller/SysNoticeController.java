package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.dto.SysNoticeDTO;
import com.swiftboot.admin.domain.entity.SysNotice;
import com.swiftboot.admin.service.SysNoticeService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.core.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统公告 Controller
 */
@Tag(name = "系统公告")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService noticeService;

    @Operation(summary = "公告列表")
    @GetMapping("/list")
    public R<List<SysNotice>> list() {
        return R.ok(noticeService.list());
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/{noticeId}")
    public R<SysNotice> getInfo(@PathVariable Long noticeId) {
        return R.ok(noticeService.getById(noticeId));
    }

    @Operation(summary = "新增公告")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysNoticeDTO noticeDTO) {
        noticeService.save(toNotice(noticeDTO));
        return R.ok();
    }

    @Operation(summary = "修改公告")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysNoticeDTO noticeDTO) {
        if (noticeDTO.getNoticeId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "公告ID不能为空");
        }
        noticeService.updateById(toNotice(noticeDTO));
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable List<Long> noticeIds) {
        noticeService.removeByIds(noticeIds);
        return R.ok();
    }

    private SysNotice toNotice(SysNoticeDTO noticeDTO) {
        SysNotice notice = new SysNotice();
        BeanUtils.copyProperties(noticeDTO, notice);
        return notice;
    }
}
