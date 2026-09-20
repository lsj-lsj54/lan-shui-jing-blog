package com.lsj.common.exception;

import com.lsj.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * 用于业务逻辑中主动抛出，如用户不存在、密码错误等
 * 全局异常处理器会捕获并返回统一的 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 仅传消息，默认错误码 FAIL(0)
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * 传错误码 + 消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 传枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 传枚举 + 自定义消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 传消息 + 原始异常（保留异常链）
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.FAIL.getCode();
    }
}