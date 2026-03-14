package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务实体
 */
@Data
@TableName("sys_job")
@Schema(description = "定时任务")
public class SysJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "job_id", type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Long jobId;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务组名")
    private String jobGroup;

    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    @Schema(description = "cron表达式")
    private String cronExpression;

    @Schema(description = "计划执行策略")
    private String misfirePolicy;

    @Schema(description = "是否并发执行")
    private String concurrent;

    @Schema(description = "状态(0正常 1暂停)")
    private String status;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updateBy;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;
}
