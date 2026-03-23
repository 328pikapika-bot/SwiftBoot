package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Job run DTO.
 */
@Data
@Schema(description = "定时任务立即执行 DTO")
public class SysJobRunDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "任务ID")
    private Long jobId;
}
