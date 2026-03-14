package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 岗位 Mapper
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {

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
}
