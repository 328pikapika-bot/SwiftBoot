package com.swiftboot.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swiftboot.test.domain.entity.TestProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目示例 Mapper
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-24 00:32:22
 */
@Mapper
public interface TestProjectMapper extends BaseMapper<TestProject> {
}
