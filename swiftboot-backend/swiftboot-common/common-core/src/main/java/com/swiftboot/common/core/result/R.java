package com.swiftboot.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结果")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码")
    private int code;

    @Schema(description = "消息")
    private String msg;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "时间戳")
    private long timestamp = System.currentTimeMillis();

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(200, msg, data);
    }

    public static <T> R<T> fail() {
        return new R<>(500, "操作失败", null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public boolean isSuccess() {
        return this.code == 200;
    }
}
