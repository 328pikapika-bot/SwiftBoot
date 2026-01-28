package com.swiftboot.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.student.domain.entity.TestStudent;
import com.swiftboot.student.mapper.TestStudentMapper;
import com.swiftboot.student.service.TestStudentService;
import com.swiftboot.common.core.domain.PageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试学生表 Service 实现
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-28 21:47:45
 */
@Service
@RequiredArgsConstructor
public class TestStudentServiceImpl extends ServiceImpl<TestStudentMapper, TestStudent> implements TestStudentService {

    @Override
    public Page<TestStudent> selectTestStudentPage(TestStudent testStudent, PageQuery pageQuery) {
        Page<TestStudent> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<TestStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(testStudent.getStudentName() != null, TestStudent::getStudentName, testStudent.getStudentName());
        wrapper.eq(testStudent.getAge() != null, TestStudent::getAge, testStudent.getAge());
        wrapper.eq(testStudent.getSex() != null, TestStudent::getSex, testStudent.getSex());
        wrapper.eq(testStudent.getBirthday() != null, TestStudent::getBirthday, testStudent.getBirthday());
        wrapper.orderByDesc(TestStudent::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void insertTestStudent(TestStudent testStudent) {
        save(testStudent);
    }

    @Override
    public void updateTestStudent(TestStudent testStudent) {
        updateById(testStudent);
    }

    @Override
    public void deleteTestStudentByIds(List<Long> ids) {
        removeByIds(ids);
    }
}
