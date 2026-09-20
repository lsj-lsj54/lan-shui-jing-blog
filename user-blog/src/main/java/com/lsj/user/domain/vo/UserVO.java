package com.lsj.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户返回视图对象
 * 排除密码等敏感字段，用于接口返回
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private String intro;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
