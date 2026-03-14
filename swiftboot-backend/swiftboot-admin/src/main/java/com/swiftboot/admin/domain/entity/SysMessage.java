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
 * 站内消息实体
 */
@Data
@TableName("sys_message")
@Schema(description = "站内消息")
public class SysMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "msg_id", type = IdType.AUTO)
    @TableField("msg_id")
    @Schema(description = "消息ID")
    private Long msgId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息类型")
    private String msgType;

    @Schema(description = "发送人ID")
    private Long senderId;

    @Schema(description = "发送人名称")
    private String senderName;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "目标ID")
    private String targetId;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "已读标志")
    private String readFlag;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
