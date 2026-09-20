package com.lsj.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeSearchDTO {

    @NotBlank(message = "关键词不能为空")
    @Size(max = 200, message = "关键词不能超过200个字符")
    private String keyword;

    /**
     * 为 true 时忽略缓存，重新调用大模型并覆盖数据库
     */
    private Boolean forceRefresh = false;
}
