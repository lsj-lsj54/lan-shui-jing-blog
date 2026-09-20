package com.lsj.article.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 分类新增 DTO
 */
@Data
public class CategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 20, message = "分类名称不能超过20个字符")
    private String name;
}
