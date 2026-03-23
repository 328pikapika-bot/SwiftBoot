package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Post write DTO.
 */
@Data
@Schema(description = "岗位写入 DTO")
public class SysPostDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "岗位ID")
    private Long postId;

    @NotBlank(message = "岗位编码不能为空")
    @Schema(description = "岗位编码")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    @Schema(description = "岗位名称")
    private String postName;

    @NotNull(message = "岗位排序不能为空")
    @Schema(description = "岗位排序")
    private Integer postSort;

    @NotBlank(message = "岗位状态不能为空")
    @Schema(description = "岗位状态")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
