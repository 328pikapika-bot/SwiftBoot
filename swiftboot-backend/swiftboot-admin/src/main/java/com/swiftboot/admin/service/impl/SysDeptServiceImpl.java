package com.swiftboot.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysDept;
import com.swiftboot.admin.mapper.SysDeptMapper;
import com.swiftboot.admin.service.SysDeptService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        return baseMapper.selectDeptTree(dept);
    }

    @Override
    public List<SysDept> selectDeptTree(SysDept dept) {
        List<SysDept> depts = selectDeptList(dept);
        return buildDeptTree(depts);
    }

    @Override
    public void insertDept(SysDept dept) {
        // 设置祖级列表
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            SysDept parent = getById(dept.getParentId());
            if (parent != null) {
                dept.setAncestors(parent.getAncestors() + "," + dept.getParentId());
            }
        } else {
            dept.setParentId(0L);
            dept.setAncestors("0");
        }
        save(dept);
    }

    @Override
    public void updateDept(SysDept dept) {
        SysDept oldDept = getById(dept.getId());
        if (oldDept == null) {
            throw new BusinessException(ResultCode.DEPT_NOT_FOUND);
        }

        // 如果父部门变更，更新祖级列表
        if (!oldDept.getParentId().equals(dept.getParentId())) {
            String newAncestors;
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                newAncestors = "0";
            } else {
                SysDept parent = getById(dept.getParentId());
                newAncestors = parent.getAncestors() + "," + dept.getParentId();
            }
            dept.setAncestors(newAncestors);

            // 更新子部门的祖级列表
            updateChildrenAncestors(dept.getId(), oldDept.getAncestors(), newAncestors);
        }

        updateById(dept);
    }

    @Override
    public void deleteDeptById(Long deptId) {
        // 检查是否有子部门
        if (baseMapper.selectChildrenCount(deptId) > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_CHILDREN);
        }
        // 检查部门下是否有用户
        if (baseMapper.selectUserCount(deptId) > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_USER);
        }
        removeById(deptId);
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        if (CollUtil.isEmpty(depts)) {
            return new ArrayList<>();
        }

        Map<Long, SysDept> deptMap = depts.stream()
                .collect(Collectors.toMap(SysDept::getId, dept -> dept));

        List<SysDept> rootDepts = new ArrayList<>();

        for (SysDept dept : depts) {
            Long parentId = dept.getParentId();
            if (parentId == null || parentId == 0L) {
                rootDepts.add(dept);
            } else {
                SysDept parent = deptMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dept);
                }
            }
        }

        return rootDepts;
    }

    /**
     * 更新子部门的祖级列表
     */
    private void updateChildrenAncestors(Long deptId, String oldAncestors, String newAncestors) {
        SysDept query = new SysDept();
        List<SysDept> children = baseMapper.selectDeptTree(query);
        for (SysDept child : children) {
            if (child.getAncestors() != null && child.getAncestors().contains(String.valueOf(deptId))) {
                child.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors));
                updateById(child);
            }
        }
    }
}
