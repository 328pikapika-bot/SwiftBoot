package com.swiftboot.admin.service;

import com.swiftboot.admin.domain.entity.SysJob;
import com.swiftboot.admin.domain.entity.SysJobLog;

import java.util.List;

/**
 * 定时任务 Service
 */
public interface SysJobService {
    List<SysJob> selectJobList(SysJob job);
    SysJob selectJobById(Long jobId);
    int insertJob(SysJob job);
    int updateJob(SysJob job);
    int deleteJobById(Long jobId);
    int changeStatus(SysJob job);
    void run(SysJob job);
}

interface SysJobLogService {
    List<SysJobLog> selectJobLogList(SysJobLog log);
    int deleteJobLogById(Long logId);
    void addJobLog(SysJob job, String status, String msg);
}
