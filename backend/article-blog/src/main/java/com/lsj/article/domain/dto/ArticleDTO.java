package com.lsj.article.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章新增/修改 DTO
 */
@Data
public class ArticleDTO {

    /**
     * 文章ID（修改时必填）
     */
    private Long id;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "文章标题不能超过100个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 摘要（为空时自动从内容截取）
     */
    @Size(max = 500, message = "摘要不能超过500个字符")
    private String summary;

    /**
     * 封面图URL
     */
    private String coverImage;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;
}
