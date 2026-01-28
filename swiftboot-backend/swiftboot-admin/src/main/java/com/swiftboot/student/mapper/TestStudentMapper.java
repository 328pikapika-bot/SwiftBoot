package com.swiftboot.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.student.domain.entity.TestStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试学生表 Mapper
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-28 21:47:45
 */
@Mapper
public interface TestStudentMapper extends BaseMapper<TestStudent> {
}
