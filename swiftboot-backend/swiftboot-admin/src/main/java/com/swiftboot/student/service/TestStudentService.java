package com.swiftboot.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.student.domain.entity.TestStudent;
import com.swiftboot.common.core.domain.PageQuery;

import java.util.List;

/**
 * 学生示例 Service
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-28 21:47:45
 */
public interface TestStudentService extends IService<TestStudent> {

    /**
     * 分页查询学生示例列表
     */
    Page<TestStudent> selectTestStudentPage(TestStudent testStudent, PageQuery pageQuery);

    /**
     * 新增学生示例
     */
    void insertTestStudent(TestStudent testStudent);

    /**
     * 修改学生示例
     */
    void updateTestStudent(TestStudent testStudent);

    /**
     * 删除学生示例
     */
    void deleteTestStudentByIds(List<Long> ids);
}
