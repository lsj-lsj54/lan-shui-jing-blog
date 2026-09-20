package com.lsj.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 消息返回视图对象
 */
@Data
public class AiMessageVO {

    private Long id;

    private Long userId;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
