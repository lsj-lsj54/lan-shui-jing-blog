package com.lsj.article.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 评论新增 DTO
 */
@Data
public class CommentDTO {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;
}
