package com.swiftboot.test.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 示例_项目表 Service
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-24 00:32:22
 */
public interface TestProjectService extends IService<TestProject> {

    /**
     * 分页查询示例_项目表列表
     */
    Page<TestProject> selectTestProjectPage(TestProject testProject, PageQuery pageQuery);

    /**
     * 新增示例_项目表
     */
    void insertTestProject(TestProject testProject);

    /**
     * 修改示例_项目表
     */
    void updateTestProject(TestProject testProject);

    /**
     * 删除示例_项目表
     */
    void deleteTestProjectByIds(List<Long> ids);
}
