package com.swiftboot.common.core.exception;

import com.swiftboot.common.core.result.ResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final HttpStatus httpStatus;

    public BusinessException(String message) {
        this(ResultCode.BAD_REQUEST.getCode(), HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(HttpStatus httpStatus, String message) {
        this(httpStatus.value(), httpStatus, message);
    }

    public BusinessException(int code, String message) {
        this(code, resolveHttpStatus(code), message);
    }

    public BusinessException(int code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resolveHttpStatus(resultCode.getCode()), resultCode.getMsg());
    }

    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), resolveHttpStatus(resultCode.getCode()), message);
    }

    public BusinessException(ResultCode resultCode, HttpStatus httpStatus, String message) {
        this(resultCode.getCode(), httpStatus, message);
    }

    private static HttpStatus resolveHttpStatus(int code) {
        if (code >= 400 && code <= 599) {
            return HttpStatus.valueOf(code);
        }
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
