package com.lsj.user.domain.vo;

import lombok.Data;

/**
 * 登录返回视图对象
 * 包含 Token 和用户信息
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息（已脱敏）
     */
    private UserVO user;
}
