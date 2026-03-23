package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysPost;
import com.swiftboot.admin.mapper.SysPostMapper;
import com.swiftboot.admin.service.SysPostService;
import com.swiftboot.common.core.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 岗位 Service 实现
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    @Override
    public List<SysPost> selectPostList(SysPost post) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(post.getPostCode())) {
            wrapper.like(SysPost::getPostCode, post.getPostCode());
        }
        if (StringUtils.hasText(post.getPostName())) {
            wrapper.like(SysPost::getPostName, post.getPostName());
        }
        if (StringUtils.hasText(post.getStatus())) {
            wrapper.eq(SysPost::getStatus, post.getStatus());
        }
        wrapper.orderByAsc(SysPost::getPostSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysPost> selectPostAll() {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getStatus, "0");
        wrapper.orderByAsc(SysPost::getPostSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysPost> selectPostListByUserId(Long userId) {
        return baseMapper.selectPostListByUserId(userId);
    }

    @Override
    public SysPost selectPostById(Long postId) {
        return baseMapper.selectById(postId);
    }

    @Override
    public int insertPost(SysPost post) {
        if (!checkPostCodeUnique(post)) {
            throw new BusinessException(HttpStatus.CONFLICT, "岗位编码已存在");
        }
        return baseMapper.insert(post);
    }

    @Override
    public int updatePost(SysPost post) {
        if (!checkPostCodeUnique(post)) {
            throw new BusinessException(HttpStatus.CONFLICT, "岗位编码已存在");
        }
        return baseMapper.updateById(post);
    }

    @Override
    public int deletePostById(Long postId) {
        return baseMapper.deleteById(postId);
    }

    @Override
    public boolean checkPostCodeUnique(SysPost post) {
        Long postId = post.getPostId() == null ? -1L : post.getPostId();
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPost::getPostCode, post.getPostCode());
        // 排除当前记录
        wrapper.ne(postId != null && postId > 0, SysPost::getPostId, postId);
        Long count = baseMapper.selectCount(wrapper);
        return count == 0;
    }
}
