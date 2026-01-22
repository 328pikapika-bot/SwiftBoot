package com.swiftboot.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.admin.domain.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询部门树
     */
    List<SysDept> selectDeptTree(@Param("dept") SysDept dept);

    /**
     * 根据ID查询子部门数量
     */
    int selectChildrenCount(@Param("deptId") Long deptId);

    /**
     * 查询部门下的用户数量
     */
    int selectUserCount(@Param("deptId") Long deptId);
}
