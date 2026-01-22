package com.swiftboot.common.log.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志事件
 */
@Data
public class OperLogEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型（0其他 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 请求参数
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（0正常 1异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime operTime;

    /**
     * 消耗时间（毫秒）
     */
    private Long costTime;
}
