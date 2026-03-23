package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.dto.SysPostDTO;
import com.swiftboot.admin.domain.entity.SysPost;
import com.swiftboot.admin.service.SysPostService;
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
    public R<Void> add(@Valid @RequestBody SysPostDTO postDTO) {
        postService.insertPost(toPost(postDTO));
        return R.ok();
    }

    @Operation(summary = "修改岗位")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysPostDTO postDTO) {
        if (postDTO.getPostId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "岗位ID不能为空");
        }
        postService.updatePost(toPost(postDTO));
        return R.ok();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{postIds}")
    public R<Void> remove(@PathVariable List<Long> postIds) {
        postIds.forEach(postService::deletePostById);
        return R.ok();
    }

    private SysPost toPost(SysPostDTO postDTO) {
        SysPost post = new SysPost();
        BeanUtils.copyProperties(postDTO, post);
        return post;
    }
}
