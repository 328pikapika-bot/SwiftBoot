package com.swiftboot.test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.test.mapper.TestProjectMapper;
import com.swiftboot.test.service.TestProjectService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目示例 Service 实现
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-24 00:32:22
 */
@Service
@RequiredArgsConstructor
public class TestProjectServiceImpl extends ServiceImpl<TestProjectMapper, TestProject> implements TestProjectService {

    @Override
    public Page<TestProject> selectTestProjectPage(TestProject testProject, PageQuery pageQuery) {
        Page<TestProject> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<TestProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(testProject.getProjectName() != null, TestProject::getProjectName, testProject.getProjectName());
        wrapper.eq(testProject.getProjectCode() != null, TestProject::getProjectCode, testProject.getProjectCode());
        wrapper.like(testProject.getManagerName() != null, TestProject::getManagerName, testProject.getManagerName());
        wrapper.orderByDesc(TestProject::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void insertTestProject(TestProject testProject) {
        save(testProject);
    }

    @Override
    public void updateTestProject(TestProject testProject) {
        updateById(testProject);
    }

    @Override
    public void deleteTestProjectByIds(List<Long> ids) {
        removeByIds(ids);
    }
}
