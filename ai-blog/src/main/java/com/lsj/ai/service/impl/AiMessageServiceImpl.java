package com.lsj.ai.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.ai.domain.dto.AiMessageDTO;
import com.lsj.ai.domain.po.AiMessage;
import com.lsj.ai.domain.vo.AiChatVO;
import com.lsj.ai.domain.vo.AiMessageVO;
import com.lsj.ai.mapper.AiMessageMapper;
import com.lsj.ai.service.DeepSeekChatService;
import com.lsj.ai.service.IAiMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage> implements IAiMessageService {

    private static final int HISTORY_LIMIT = 8;

    private final DeepSeekChatService deepSeekChatService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatVO sendMessage(AiMessageDTO dto, Long userId) {
        List<AiMessage> history = lambdaQuery()
                .eq(AiMessage::getUserId, userId)
                .orderByDesc(AiMessage::getCreateTime)
                .last("LIMIT " + HISTORY_LIMIT)
                .list();
        Collections.reverse(history);

        AiMessage userMessage = new AiMessage();
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        save(userMessage);

        String reply = deepSeekChatService.chat(dto.getContent(), history);

        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        save(assistantMessage);

        log.info("AI 对话完成: userId={}, userMessageId={}, assistantMessageId={}",
                userId, userMessage.getId(), assistantMessage.getId());

        AiChatVO vo = new AiChatVO();
        vo.setUserMessage(toVO(userMessage));
        vo.setAssistantMessage(toVO(assistantMessage));
        return vo;
    }

    @Override
    public List<AiMessageVO> listByUserId(Long userId) {
        List<AiMessage> messages = lambdaQuery()
                .eq(AiMessage::getUserId, userId)
                .orderByAsc(AiMessage::getCreateTime)
                .list();
        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AiMessageVO toVO(AiMessage message) {
        if (message == null) {
            return null;
        }
        AiMessageVO vo = new AiMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
