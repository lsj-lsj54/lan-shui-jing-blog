package com.lsj.ai.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.ai.domain.dto.AiMessageDTO;
import com.lsj.ai.domain.po.AiMessage;
import com.lsj.ai.domain.vo.AiChatVO;
import com.lsj.ai.domain.vo.AiMessageVO;

import java.util.List;

public interface IAiMessageService extends IService<AiMessage> {

    /**
     * 发送消息：保存用户消息，调用 DeepSeek，再保存助手回复
     */
    AiChatVO sendMessage(AiMessageDTO dto, Long userId);

    /**
     * 根据用户ID查询对话历史
     */
    List<AiMessageVO> listByUserId(Long userId);

    /**
     * PO 转 VO
     */
    AiMessageVO toVO(AiMessage message);
}
