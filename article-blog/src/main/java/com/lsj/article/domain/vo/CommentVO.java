package com.lsj.article.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论返回视图对象
 */
@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private Long userId;

    private String username;

    private String content;

    private LocalDateTime createTime;
}
