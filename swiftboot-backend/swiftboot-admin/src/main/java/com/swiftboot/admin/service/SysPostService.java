package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.entity.SysPost;

import java.util.List;

/**
 * 岗位 Service
 */
public interface SysPostService {

    /**
     * 查询岗位列表
     */
    List<SysPost> selectPostList(SysPost post);

    /**
     * 查询所有启用的岗位
     */
    List<SysPost> selectPostAll();

    /**
     * 根据用户ID查询岗位
     */
    List<SysPost> selectPostListByUserId(Long userId);

    /**
     * 根据岗位ID查询岗位
     */
    SysPost selectPostById(Long postId);

    /**
     * 新增岗位
     */
    int insertPost(SysPost post);

    /**
     * 修改岗位
     */
    int updatePost(SysPost post);

    /**
     * 删除岗位
     */
    int deletePostById(Long postId);

    /**
     * 校验岗位编码是否唯一
     */
    boolean checkPostCodeUnique(SysPost post);
}
