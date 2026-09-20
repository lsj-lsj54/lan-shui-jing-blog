package com.lsj.ai.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 消息新增 DTO
 */
@Data
public class AiMessageDTO {

    /**
     * 角色：user-用户, assistant-AI助手。对话接口可不传，默认 user
     */
    private String role;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过4000个字符")
    private String content;
}
