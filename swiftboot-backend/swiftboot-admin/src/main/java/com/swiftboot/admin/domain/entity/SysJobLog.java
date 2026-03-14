package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务日志实体
 */
@Data
@TableName("sys_job_log")
@Schema(description = "定时任务日志")
public class SysJobLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "任务ID")
    private Long jobId;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务组名")
    private String jobGroup;

    @Schema(description = "调用目标字符串")
    private String invokeTarget;

    @Schema(description = "日志信息")
    private String jobMessage;

    @Schema(description = "执行状态(0成功 1失败)")
    private String status;

    @Schema(description = "异常信息")
    private String exceptionInfo;

    @Schema(description = "创建时间")
    private Date createTime;
}
