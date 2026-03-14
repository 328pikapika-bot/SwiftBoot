package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.entity.SysMessage;
import com.swiftboot.admin.service.SysMessageService;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 站内消息 Controller
 */
@Tag(name = "站内消息")
@RestController
@RequestMapping("/system/message")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageService messageService;

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public R<List<SysMessage>> list() {
        return R.ok(messageService.list());
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/{msgId}")
    public R<SysMessage> getInfo(@PathVariable Long msgId) {
        return R.ok(messageService.getById(msgId));
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public R<Void> add(@RequestBody SysMessage message) {
        messageService.save(message);
        return R.ok();
    }

    @Operation(summary = "修改消息")
    @PutMapping
    public R<Void> edit(@RequestBody SysMessage message) {
        messageService.updateById(message);
        return R.ok();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{msgIds}")
    public R<Void> remove(@PathVariable Long[] msgIds) {
        messageService.removeByIds(Arrays.asList(msgIds));
        return R.ok();
    }
}
