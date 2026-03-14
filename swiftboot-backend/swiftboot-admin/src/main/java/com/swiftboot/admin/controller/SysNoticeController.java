package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.entity.SysNotice;
import com.swiftboot.admin.service.SysNoticeService;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
    public R<Void> add(@RequestBody SysNotice notice) {
        noticeService.save(notice);
        return R.ok();
    }

    @Operation(summary = "修改公告")
    @PutMapping
    public R<Void> edit(@RequestBody SysNotice notice) {
        noticeService.updateById(notice);
        return R.ok();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        noticeService.removeByIds(Arrays.asList(noticeIds));
        return R.ok();
    }
}
