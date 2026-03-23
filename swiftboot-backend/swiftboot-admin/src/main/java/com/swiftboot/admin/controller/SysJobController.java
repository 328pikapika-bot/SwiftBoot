package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.dto.SysJobDTO;
import com.swiftboot.admin.domain.dto.SysJobRunDTO;
import com.swiftboot.admin.domain.dto.SysJobStatusDTO;
import com.swiftboot.admin.domain.entity.SysJob;
import com.swiftboot.admin.service.SysJobService;
import com.swiftboot.common.core.exception.BusinessException;
import com.swiftboot.common.core.result.R;
import com.swiftboot.common.core.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 定时任务 Controller
 */
@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final SysJobService jobService;

    @Operation(summary = "任务列表")
    @GetMapping("/list")
    public R<List<SysJob>> list(SysJob job) {
        return R.ok(jobService.selectJobList(job));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{jobId}")
    public R<SysJob> getInfo(@PathVariable Long jobId) {
        return R.ok(jobService.selectJobById(jobId));
    }

    @Operation(summary = "新增任务")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysJobDTO jobDTO) {
        jobService.insertJob(toJob(jobDTO));
        return R.ok();
    }

    @Operation(summary = "修改任务")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysJobDTO jobDTO) {
        if (jobDTO.getJobId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "任务ID不能为空");
        }
        jobService.updateJob(toJob(jobDTO));
        return R.ok();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable List<Long> jobIds) {
        jobIds.forEach(jobService::deleteJobById);
        return R.ok();
    }

    @Operation(summary = "修改任务状态")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Valid @RequestBody SysJobStatusDTO jobStatusDTO) {
        SysJob job = new SysJob();
        job.setJobId(jobStatusDTO.getJobId());
        job.setStatus(jobStatusDTO.getStatus());
        jobService.changeStatus(job);
        return R.ok();
    }

    @Operation(summary = "立即执行任务")
    @PutMapping("/run")
    public R<Void> run(@Valid @RequestBody SysJobRunDTO jobRunDTO) {
        SysJob job = new SysJob();
        job.setJobId(jobRunDTO.getJobId());
        jobService.run(job);
        return R.ok();
    }

    private SysJob toJob(SysJobDTO jobDTO) {
        SysJob job = new SysJob();
        BeanUtils.copyProperties(jobDTO, job);
        return job;
    }
}
