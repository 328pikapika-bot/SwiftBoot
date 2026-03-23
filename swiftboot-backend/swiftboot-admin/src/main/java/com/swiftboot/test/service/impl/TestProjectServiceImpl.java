package com.swiftboot.test.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.common.core.domain.PageQuery;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.util.excel.ExcelUtils;
import com.swiftboot.test.domain.entity.TestProject;
import com.swiftboot.test.domain.excel.TestProjectExcelVO;
import com.swiftboot.test.domain.vo.TestProjectImportResultVO;
import com.swiftboot.test.mapper.TestProjectMapper;
import com.swiftboot.test.service.TestProjectService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 示例项目服务实现
 */
@Service
@RequiredArgsConstructor
public class TestProjectServiceImpl extends ServiceImpl<TestProjectMapper, TestProject> implements TestProjectService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Map<Integer, String> PROJECT_TYPE_MAP = new LinkedHashMap<>();
    private static final Map<Integer, String> STATUS_MAP = new LinkedHashMap<>();
    private static final Map<Integer, String> PRIORITY_MAP = new LinkedHashMap<>();

    static {
        PROJECT_TYPE_MAP.put(1, "内部项目");
        PROJECT_TYPE_MAP.put(2, "外包项目");
        PROJECT_TYPE_MAP.put(3, "合作项目");

        STATUS_MAP.put(0, "进行中");
        STATUS_MAP.put(1, "已完成");
        STATUS_MAP.put(2, "已暂停");
        STATUS_MAP.put(3, "已取消");

        PRIORITY_MAP.put(1, "低");
        PRIORITY_MAP.put(2, "中");
        PRIORITY_MAP.put(3, "高");
    }

    @Override
    public Page<TestProject> selectTestProjectPage(TestProject testProject, PageQuery pageQuery) {
        Page<TestProject> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        return page(page, buildQueryWrapper(testProject, null));
    }

    @Override
    public List<TestProject> selectTestProjectList(TestProject testProject, List<Long> ids) {
        return list(buildQueryWrapper(testProject, ids));
    }

    @Override
    public void insertTestProject(TestProject testProject) {
        prepareProject(testProject);
        validateProjectCode(testProject.getProjectCode(), null);
        save(testProject);
    }

    @Override
    public void updateTestProject(TestProject testProject) {
        prepareProject(testProject);
        validateProjectCode(testProject.getProjectCode(), testProject.getId());
        updateById(testProject);
    }

    @Override
    public void deleteTestProjectByIds(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    public void exportTestProject(TestProject testProject, List<Long> ids, HttpServletResponse response) {
        List<TestProject> projects = selectTestProjectList(testProject, ids);
        List<TestProjectExcelVO> exportRows = projects.stream().map(this::toExcelRow).toList();
        ExcelUtils.exportExcel(response, exportRows, "示例项目数据", TestProjectExcelVO.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public TestProjectImportResultVO importTestProject(MultipartFile file, boolean updateSupport) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("导入文件不能为空");
        }

        final List<TestProjectExcelVO> rows;
        try {
            rows = (List<TestProjectExcelVO>) (List<?>) ExcelUtils.importExcel(file.getInputStream(), TestProjectExcelVO.class);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "导入文件读取失败: " + e.getMessage());
        }

        TestProjectImportResultVO result = new TestProjectImportResultVO();
        if (CollUtil.isEmpty(rows)) {
            return result;
        }

        int rowNum = 1;
        for (TestProjectExcelVO row : rows) {
            rowNum++;
            if (isEmptyRow(row)) {
                continue;
            }
            try {
                TestProject project = toEntity(row);
                TestProject existing = getByProjectCode(project.getProjectCode());
                if (existing != null) {
                    if (!updateSupport) {
                        throw new BusinessException("项目编号已存在: " + project.getProjectCode());
                    }
                    project.setId(existing.getId());
                    project.setCreateBy(existing.getCreateBy());
                    project.setCreateTime(existing.getCreateTime());
                    updateTestProject(project);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                    result.setUpdateCount(result.getUpdateCount() + 1);
                } else {
                    insertTestProject(project);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
            } catch (Exception e) {
                result.setFailureCount(result.getFailureCount() + 1);
                result.getFailureMessages().add("第" + rowNum + "行导入失败: " + e.getMessage());
            }
        }
        return result;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        TestProjectExcelVO sample = new TestProjectExcelVO();
        sample.setProjectName("供应链中台升级");
        sample.setProjectCode("PRJ-2026-001");
        sample.setProjectType("内部项目");
        sample.setManagerName("张敏");
        sample.setStartDate("2026-03-01");
        sample.setEndDate("2026-09-30");
        sample.setBudget("850000");
        sample.setProgress("35");
        sample.setStatus("进行中");
        sample.setPriority("高");
        sample.setDescription("支持文本或数字枚举值，例如项目类型可填写 内部项目/外包项目/合作项目 或 1/2/3");
        sample.setRemark("导入时将按项目编号判重");
        ExcelUtils.exportExcel(response, List.of(sample), "示例项目导入模板", TestProjectExcelVO.class);
    }

    private LambdaQueryWrapper<TestProject> buildQueryWrapper(TestProject testProject, List<Long> ids) {
        String projectName = trimToNull(testProject.getProjectName());
        String projectCode = trimToNull(testProject.getProjectCode());
        String managerName = trimToNull(testProject.getManagerName());
        LambdaQueryWrapper<TestProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(hasText(projectName), TestProject::getProjectName, projectName);
        wrapper.eq(hasText(projectCode), TestProject::getProjectCode, projectCode);
        wrapper.like(hasText(managerName), TestProject::getManagerName, managerName);
        wrapper.eq(testProject.getProjectType() != null, TestProject::getProjectType, testProject.getProjectType());
        wrapper.eq(testProject.getStatus() != null, TestProject::getStatus, testProject.getStatus());
        wrapper.eq(testProject.getPriority() != null, TestProject::getPriority, testProject.getPriority());
        wrapper.in(CollUtil.isNotEmpty(ids), TestProject::getId, ids);
        wrapper.orderByDesc(TestProject::getCreateTime, TestProject::getId);
        return wrapper;
    }

    private void prepareProject(TestProject testProject) {
        if (hasText(testProject.getProjectName())) {
            testProject.setProjectName(testProject.getProjectName().trim());
        }
        if (hasText(testProject.getProjectCode())) {
            testProject.setProjectCode(testProject.getProjectCode().trim());
        }
        if (hasText(testProject.getManagerName())) {
            testProject.setManagerName(testProject.getManagerName().trim());
        }
        if (testProject.getProgress() == null) {
            testProject.setProgress(0);
        }
        if (testProject.getStatus() == null) {
            testProject.setStatus(0);
        }
        if (testProject.getPriority() == null) {
            testProject.setPriority(2);
        }
        if (testProject.getBudget() == null) {
            testProject.setBudget(BigDecimal.ZERO);
        }
        if (testProject.getBudget().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("项目预算不能小于0");
        }
        if (testProject.getStartDate() != null && testProject.getEndDate() != null
                && testProject.getEndDate().isBefore(testProject.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }

    private void validateProjectCode(String projectCode, Long excludeId) {
        LambdaQueryWrapper<TestProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestProject::getProjectCode, projectCode);
        wrapper.ne(excludeId != null, TestProject::getId, excludeId);
        if (count(wrapper) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "项目编号已存在");
        }
    }

    private TestProject getByProjectCode(String projectCode) {
        if (!hasText(projectCode)) {
            return null;
        }
        LambdaQueryWrapper<TestProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestProject::getProjectCode, projectCode.trim());
        wrapper.last("limit 1");
        return getOne(wrapper, false);
    }

    private TestProjectExcelVO toExcelRow(TestProject project) {
        TestProjectExcelVO row = new TestProjectExcelVO();
        row.setProjectName(project.getProjectName());
        row.setProjectCode(project.getProjectCode());
        row.setProjectType(labelOf(PROJECT_TYPE_MAP, project.getProjectType()));
        row.setManagerName(project.getManagerName());
        row.setStartDate(formatDate(project.getStartDate()));
        row.setEndDate(formatDate(project.getEndDate()));
        row.setBudget(project.getBudget() == null ? "" : project.getBudget().stripTrailingZeros().toPlainString());
        row.setProgress(project.getProgress() == null ? "" : String.valueOf(project.getProgress()));
        row.setStatus(labelOf(STATUS_MAP, project.getStatus()));
        row.setPriority(labelOf(PRIORITY_MAP, project.getPriority()));
        row.setDescription(project.getDescription());
        row.setRemark(project.getRemark());
        return row;
    }

    private TestProject toEntity(TestProjectExcelVO row) {
        TestProject project = new TestProject();
        project.setProjectName(trimToNull(row.getProjectName()));
        project.setProjectCode(trimToNull(row.getProjectCode()));
        project.setProjectType(parseDictValue(row.getProjectType(), PROJECT_TYPE_MAP, "项目类型"));
        project.setManagerName(trimToNull(row.getManagerName()));
        project.setStartDate(parseDate(row.getStartDate(), "开始日期"));
        project.setEndDate(parseDate(row.getEndDate(), "结束日期"));
        project.setBudget(parseDecimal(row.getBudget(), "项目预算"));
        project.setProgress(parseInteger(row.getProgress(), "项目进度"));
        project.setStatus(parseDictValue(row.getStatus(), STATUS_MAP, "项目状态"));
        project.setPriority(parseDictValue(row.getPriority(), PRIORITY_MAP, "优先级"));
        project.setDescription(trimToNull(row.getDescription()));
        project.setRemark(trimToNull(row.getRemark()));
        if (!hasText(project.getProjectName())) {
            throw new BusinessException("项目名称不能为空");
        }
        if (!hasText(project.getProjectCode())) {
            throw new BusinessException("项目编号不能为空");
        }
        prepareProject(project);
        return project;
    }

    private Integer parseDictValue(String value, Map<Integer, String> dictMap, String fieldName) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        for (Map.Entry<Integer, String> entry : dictMap.entrySet()) {
            if (Objects.equals(entry.getValue(), text) || Objects.equals(String.valueOf(entry.getKey()), text)) {
                return entry.getKey();
            }
        }
        throw new BusinessException(fieldName + "取值不合法: " + text);
    }

    private LocalDate parseDate(String value, String fieldName) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(fieldName + "格式错误，正确格式为 yyyy-MM-dd");
        }
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        String text = trimToNull(value);
        if (text == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + "格式错误");
        }
    }

    private Integer parseInteger(String value, String fieldName) {
        String text = trimToNull(value);
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + "格式错误");
        }
    }

    private boolean isEmptyRow(TestProjectExcelVO row) {
        return !hasText(row.getProjectName())
                && !hasText(row.getProjectCode())
                && !hasText(row.getManagerName())
                && !hasText(row.getDescription());
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }

    private String labelOf(Map<Integer, String> dictMap, Integer value) {
        if (value == null) {
            return "";
        }
        return dictMap.getOrDefault(value, String.valueOf(value));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
