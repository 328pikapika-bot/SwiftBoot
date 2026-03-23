package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Job write DTO.
 */
@Data
@Schema(description = "定时任务写入 DTO")
public class SysJobDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long jobId;

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称")
    private String jobName;

    @NotBlank(message = "任务分组不能为空")
    @Schema(description = "任务分组")
    private String jobGroup;

    @NotBlank(message = "调用目标不能为空")
    @Schema(description = "调用目标")
    private String invokeTarget;

    @NotBlank(message = "Cron 表达式不能为空")
    @Schema(description = "Cron 表达式")
    private String cronExpression;

    @Schema(description = "计划执行策略")
    private String misfirePolicy;

    @Schema(description = "是否并发执行")
    private String concurrent;

    @NotBlank(message = "任务状态不能为空")
    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
