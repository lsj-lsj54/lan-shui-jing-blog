package com.lsj.ai.service;

import com.lsj.ai.domain.dto.KnowledgeGeneration;
import com.lsj.ai.domain.po.AiMessage;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekChatService {

    private static final String KNOWLEDGE_SYSTEM = """
            你是资深软件工程师和技术文档作者。用户会给出一个技术关键词（类名、方法名、API 或概念）。
            请生成结构化技术说明，必须使用中文。
            返回字段：
            - title: 简洁标题
            - summary: 一两句话摘要
            - content: Markdown 正文，依次包含「作用与用法」「关键参数/返回值」「代码示例」「注意事项」
            要求准确，不要编造不存在的 API。如果不确定请明确说明。
            """;

    private final ObjectProvider<ChatClient> chatClientProvider;

    public String chat(String userMessage, List<AiMessage> history) {
        ChatClient client = requireClient();
        try {
            List<Message> messages = new ArrayList<>();
            if (history != null) {
                for (AiMessage item : history) {
                    if (!StringUtils.hasText(item.getContent())) {
                        continue;
                    }
                    if ("assistant".equalsIgnoreCase(item.getRole())) {
                        messages.add(new AssistantMessage(item.getContent()));
                    } else {
                        messages.add(new UserMessage(item.getContent()));
                    }
                }
            }
            String content = client.prompt()
                    .messages(messages)
                    .user(userMessage)
                    .call()
                    .content();
            if (!StringUtils.hasText(content)) {
                throw new BusinessException(ResultCode.AI_CALL_FAILED, "模型未返回内容");
            }
            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek 对话调用失败", e);
            throw new BusinessException(ResultCode.AI_CALL_FAILED, "AI 调用失败：" + e.getMessage());
        }
    }

    public KnowledgeGeneration generateKnowledge(String keyword) {
        ChatClient client = requireClient();
        try {
            KnowledgeGeneration generation = client.prompt()
                    .system(KNOWLEDGE_SYSTEM)
                    .user("请解释这个技术关键词，并给出可落地的用法说明：\n" + keyword)
                    .call()
                    .entity(KnowledgeGeneration.class);
            if (generation == null || !StringUtils.hasText(generation.getContent())) {
                String markdown = client.prompt()
                        .system(KNOWLEDGE_SYSTEM)
                        .user("请用 Markdown 解释：" + keyword)
                        .call()
                        .content();
                generation = fallback(keyword, markdown);
            }
            if (!StringUtils.hasText(generation.getTitle())) {
                generation.setTitle(keyword);
            }
            if (!StringUtils.hasText(generation.getSummary())) {
                generation.setSummary(abbreviate(generation.getContent(), 180));
            }
            return generation;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek 知识生成失败, keyword={}", keyword, e);
            throw new BusinessException(ResultCode.AI_CALL_FAILED, "知识生成失败：" + e.getMessage());
        }
    }

    private ChatClient requireClient() {
        ChatClient client = chatClientProvider.getIfAvailable();
        if (client == null) {
            throw new BusinessException(ResultCode.AI_NOT_CONFIGURED);
        }
        return client;
    }

    private KnowledgeGeneration fallback(String keyword, String markdown) {
        if (!StringUtils.hasText(markdown)) {
            throw new BusinessException(ResultCode.AI_CALL_FAILED, "模型未返回知识内容");
        }
        KnowledgeGeneration generation = new KnowledgeGeneration();
        generation.setTitle(keyword);
        generation.setSummary(abbreviate(markdown, 180));
        generation.setContent(markdown);
        return generation;
    }

    private String abbreviate(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max) + "...";
    }
}
