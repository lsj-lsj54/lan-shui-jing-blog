package com.lsj.article.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章返回视图对象
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String content;

    /**
     * 当前用户是否已点赞（非数据库字段）
     */
    private Boolean isLiked;

    private String tags;

    private String summary;

    private String coverImage;

    private Long userId;

    private Long categoryId;

    private Integer status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private String author;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
