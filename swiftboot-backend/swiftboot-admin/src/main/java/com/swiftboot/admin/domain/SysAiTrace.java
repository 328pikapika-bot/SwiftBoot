package com.swiftboot.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 可解释性 Trace 记录对象 sys_ai_trace
 *
 * @author swiftboot
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_ai_trace")
public class SysAiTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** Trace ID */
    private String traceId;

    /** 推理路径 (JSON) */
    private String thoughtPath;

    /** 工具调用记录 (JSON) */
    private String toolCalls;

    /** 上下文信息 */
    private String contextInfo;

    /** 最终回答 */
    private String finalAnswer;

    /** 总耗时 (ms) */
    private Long duration;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 删除标志（0代表存在 1代表删除） */
    @TableLogic
    private Integer deleted;
}
