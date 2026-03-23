package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Notice write DTO.
 */
@Data
@Schema(description = "公告写入 DTO")
public class SysNoticeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "公告ID")
    private Long noticeId;

    @NotBlank(message = "公告标题不能为空")
    @Schema(description = "公告标题")
    private String noticeTitle;

    @NotBlank(message = "公告类型不能为空")
    @Schema(description = "公告类型")
    private String noticeType;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容")
    private String noticeContent;

    @NotNull(message = "公告状态不能为空")
    @Schema(description = "公告状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
