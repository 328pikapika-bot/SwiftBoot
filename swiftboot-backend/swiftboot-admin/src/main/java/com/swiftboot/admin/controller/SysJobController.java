package com.swiftboot.admin.controller;

import com.swiftboot.admin.domain.entity.SysJob;
import com.swiftboot.admin.service.SysJobService;
import com.swiftboot.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
    public R<Void> add(@RequestBody SysJob job) {
        jobService.insertJob(job);
        return R.ok();
    }

    @Operation(summary = "修改任务")
    @PutMapping
    public R<Void> edit(@RequestBody SysJob job) {
        jobService.updateJob(job);
        return R.ok();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{jobIds}")
    public R<Void> remove(@PathVariable Long[] jobIds) {
        Arrays.asList(jobIds).forEach(id -> jobService.deleteJobById(id));
        return R.ok();
    }

    @Operation(summary = "修改任务状态")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysJob job) {
        jobService.changeStatus(job);
        return R.ok();
    }

    @Operation(summary = "立即执行任务")
    @PutMapping("/run")
    public R<Void> run(@RequestBody SysJob job) {
        jobService.run(job);
        return R.ok();
    }
}
