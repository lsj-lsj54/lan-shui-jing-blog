package com.lsj.article.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    @TableField(exist = false)
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

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String author;
}
