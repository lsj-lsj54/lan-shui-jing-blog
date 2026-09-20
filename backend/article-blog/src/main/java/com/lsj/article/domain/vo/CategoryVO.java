package com.lsj.article.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类返回视图对象
 */
@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Long userId;

    private LocalDateTime createTime;
}
