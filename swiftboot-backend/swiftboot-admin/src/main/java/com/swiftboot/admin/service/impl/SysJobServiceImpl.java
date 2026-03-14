package com.swiftboot.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swiftboot.admin.domain.entity.SysJob;
import com.swiftboot.admin.domain.entity.SysJobLog;
import com.swiftboot.admin.mapper.SysJobLogMapper;
import com.swiftboot.admin.mapper.SysJobMapper;
import com.swiftboot.admin.service.SysJobService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 定时任务 Service 实现
 */
@Service
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(job.getJobName())) {
            wrapper.like(SysJob::getJobName, job.getJobName());
        }
        if (StringUtils.hasText(job.getJobGroup())) {
            wrapper.eq(SysJob::getJobGroup, job.getJobGroup());
        }
        if (StringUtils.hasText(job.getStatus())) {
            wrapper.eq(SysJob::getStatus, job.getStatus());
        }
        wrapper.orderByDesc(SysJob::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return baseMapper.selectById(jobId);
    }

    @Override
    public int insertJob(SysJob job) {
        return baseMapper.insert(job);
    }

    @Override
    public int updateJob(SysJob job) {
        return baseMapper.updateById(job);
    }

    @Override
    public int deleteJobById(Long jobId) {
        return baseMapper.deleteById(jobId);
    }

    @Override
    public int changeStatus(SysJob job) {
        SysJob j = new SysJob();
        j.setJobId(job.getJobId());
        j.setStatus(job.getStatus());
        return baseMapper.updateById(j);
    }

    @Override
    public void run(SysJob job) {
        // TODO: 实际执行任务逻辑
        System.out.println("执行任务: " + job.getJobName());
    }
}
