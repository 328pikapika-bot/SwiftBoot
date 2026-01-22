package com.swiftboot.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.admin.domain.entity.SysDept;

import java.util.List;

/**
 * 部门 Service
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询部门列表
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 查询部门树
     */
    List<SysDept> selectDeptTree(SysDept dept);

    /**
     * 新增部门
     */
    void insertDept(SysDept dept);

    /**
     * 修改部门
     */
    void updateDept(SysDept dept);

    /**
     * 删除部门
     */
    void deleteDeptById(Long deptId);

    /**
     * 构建部门树
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);
}
