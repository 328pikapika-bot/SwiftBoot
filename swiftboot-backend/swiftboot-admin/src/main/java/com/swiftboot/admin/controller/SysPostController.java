package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.entity.SysPost;
import com.swiftboot.admin.service.SysPostService;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 岗位 Controller
 */
@Tag(name = "岗位管理")
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService postService;

    @Operation(summary = "岗位列表")
    @GetMapping("/list")
    public R<List<SysPost>> list(SysPost post) {
        return R.ok(postService.selectPostList(post));
    }

    @Operation(summary = "获取岗位详情")
    @GetMapping("/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        return R.ok(postService.selectPostById(postId));
    }

    @Operation(summary = "获取所有启用的岗位")
    @GetMapping("/all")
    public R<List<SysPost>> getAll() {
        return R.ok(postService.selectPostAll());
    }

    @Operation(summary = "新增岗位")
    @PostMapping
    public R<Void> add(@RequestBody SysPost post) {
        if (!postService.checkPostCodeUnique(post)) {
            return R.fail("岗位编码已存在");
        }
        postService.insertPost(post);
        return R.ok();
    }

    @Operation(summary = "修改岗位")
    @PutMapping
    public R<Void> edit(@RequestBody SysPost post) {
        if (!postService.checkPostCodeUnique(post)) {
            return R.fail("岗位编码已存在");
        }
        postService.updatePost(post);
        return R.ok();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable Long[] postIds) {
        Arrays.asList(postIds).forEach(id -> postService.deletePostById(id));
        return R.ok();
    }
}
