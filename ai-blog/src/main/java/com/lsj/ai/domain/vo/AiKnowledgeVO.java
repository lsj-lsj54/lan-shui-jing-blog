package com.lsj.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeVO {

    private Long id;

    private Long userId;

    private String keyword;

    private String keywordRaw;

    private String title;

    private String summary;

    private String content;

    private String model;

    /**
     * 本次是否命中已有知识（未重新调用大模型）
     */
    private Boolean cached;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
