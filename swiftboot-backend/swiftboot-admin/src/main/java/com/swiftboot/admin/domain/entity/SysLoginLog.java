package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志
 */
@Data
@TableName("sys_login_log")
@Schema(description = "登录日志")
public class SysLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器类型")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录状态（0成功 1失败）")
    private Integer status;

    @Schema(description = "提示消息")
    private String msg;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
