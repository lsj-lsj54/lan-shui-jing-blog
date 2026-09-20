package com.lsj.common.result;

import com.lsj.common.enums.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data                // 自动生成 getter/setter
@NoArgsConstructor   // 自动生成无参构造函数（Jackson 实例化需要）
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功，无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功，自定义消息
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败，默认错误
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg(), null);
    }

    /**
     * 失败，自定义消息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 失败，自定义状态码 + 消息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败，使用枚举
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 失败，使用枚举 + 自定义消息
     */
    public static <T> Result<T> error(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }
}
