package com.swiftboot.test.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.test.domain.vo.TestProjectImportResultVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 示例项目服务
 */
public interface TestProjectService extends IService<TestProject> {

    /**
     * 分页查询示例项目列表
     */
    Page<TestProject> selectTestProjectPage(TestProject testProject, PageQuery pageQuery);

    /**
     * 查询示例项目列表
     */
    List<TestProject> selectTestProjectList(TestProject testProject, List<Long> ids);

    /**
     * 新增示例项目
     */
    void insertTestProject(TestProject testProject);

    /**
     * 修改示例项目
     */
    void updateTestProject(TestProject testProject);

    /**
     * 删除示例项目
     */
    void deleteTestProjectByIds(List<Long> ids);

    /**
     * 导出示例项目
     */
    void exportTestProject(TestProject testProject, List<Long> ids, HttpServletResponse response);

    /**
     * 导入示例项目
     */
    TestProjectImportResultVO importTestProject(MultipartFile file, boolean updateSupport);

    /**
     * 下载导入模板
     */
    void downloadImportTemplate(HttpServletResponse response);
}
