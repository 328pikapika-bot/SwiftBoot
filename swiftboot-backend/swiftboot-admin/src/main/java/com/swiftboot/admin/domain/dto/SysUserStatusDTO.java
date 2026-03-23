package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * User status DTO.
 */
@Data
@Schema(description = "用户状态变更 DTO")
public class SysUserStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long id;

    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态")
    private Integer status;
}
