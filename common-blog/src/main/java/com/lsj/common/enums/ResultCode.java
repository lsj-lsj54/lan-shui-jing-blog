package com.lsj.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {

    // 成功
    SUCCESS(1, "操作成功"),

    // 失败
    FAIL(0, "操作失败"),

    // 参数错误 4000-4999
    PARAM_ERROR(4000, "参数错误"),
    PARAM_NULL(4001, "参数不能为空"),

    // 用户错误 5000-5999
    USER_NOT_FOUND(5000, "用户不存在"),
    USER_EXISTS(5001, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(5002, "用户名或密码错误"),
    UNAUTHORIZED(5003, "未登录或登录已过期"),

    // 文章错误 6000-6999
    ARTICLE_NOT_FOUND(6000, "文章不存在"),

    // AI 错误 7000-7999
    AI_CALL_FAILED(7000, "AI 服务调用失败"),
    AI_NOT_CONFIGURED(7001, "未配置 DeepSeek API Key"),
    KNOWLEDGE_NOT_FOUND(7002, "知识条目不存在"),

    // 系统错误 9000-9999
    SYSTEM_ERROR(9000, "系统异常");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
