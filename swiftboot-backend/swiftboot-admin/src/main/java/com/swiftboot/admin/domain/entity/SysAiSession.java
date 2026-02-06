package com.swiftboot.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 智能会话实体类
 */
@Data
@TableName("sys_ai_session")
public class SysAiSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "提问用户ID")
    private Long userId;

    @Schema(description = "用户提问内容")
    private String question;

    @Schema(description = "AI回复内容")
    private String answer;

    @Schema(description = "使用的模型")
    private String model;

    @Schema(description = "消耗Token数")
    private Integer tokens;

    @Schema(description = "耗时(毫秒)")
    private Integer duration;

    @Schema(description = "提问时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "用户名称")
    @TableField(exist = false)
    private String username;
}
