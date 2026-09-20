package com.lsj.ai.domain.vo;

import lombok.Data;

@Data
public class AiChatVO {

    private AiMessageVO userMessage;

    private AiMessageVO assistantMessage;
}
