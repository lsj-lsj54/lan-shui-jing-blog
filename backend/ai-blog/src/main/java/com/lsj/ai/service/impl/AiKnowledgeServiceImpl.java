package com.lsj.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.ai.domain.dto.KnowledgeGeneration;
import com.lsj.ai.domain.dto.KnowledgeSearchDTO;
import com.lsj.ai.domain.po.AiKnowledge;
import com.lsj.ai.domain.vo.AiKnowledgeVO;
import com.lsj.ai.mapper.AiKnowledgeMapper;
import com.lsj.ai.service.DeepSeekChatService;
import com.lsj.ai.service.IAiKnowledgeService;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge> implements IAiKnowledgeService {

    private final DeepSeekChatService deepSeekChatService;

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String model;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiKnowledgeVO searchAndSave(KnowledgeSearchDTO dto, Long userId) {
        String raw = dto.getKeyword().trim();
        String keyword = normalize(raw);
        boolean forceRefresh = Boolean.TRUE.equals(dto.getForceRefresh());

        AiKnowledge existing = lambdaQuery()
                .eq(AiKnowledge::getKeyword, keyword)
                .one();
        if (existing != null && !forceRefresh) {
            log.info("知识命中缓存: keyword={}, id={}", keyword, existing.getId());
            return toVO(existing, true);
        }

        KnowledgeGeneration generation = deepSeekChatService.generateKnowledge(raw);
        AiKnowledge entity = existing == null ? new AiKnowledge() : existing;
        entity.setUserId(userId);
        entity.setKeyword(keyword);
        entity.setKeywordRaw(raw);
        entity.setTitle(generation.getTitle());
        entity.setSummary(generation.getSummary());
        entity.setContent(generation.getContent());
        entity.setModel(model);
        saveOrUpdate(entity);
        log.info("知识已写入数据库: keyword={}, id={}, refresh={}", keyword, entity.getId(), forceRefresh);
        return toVO(entity, false);
    }

    @Override
    public Page<AiKnowledgeVO> pageKnowledge(Integer pageNum, Integer pageSize, String keyword) {
        Page<AiKnowledge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
                .like(AiKnowledge::getKeywordRaw, keyword)
                .or()
                .like(AiKnowledge::getTitle, keyword)
                .or()
                .like(AiKnowledge::getSummary, keyword));
        wrapper.orderByDesc(AiKnowledge::getUpdateTime);
        Page<AiKnowledge> result = page(page, wrapper);
        Page<AiKnowledgeVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(item -> toVO(item, true))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public AiKnowledgeVO getDetail(Long id) {
        AiKnowledge knowledge = getById(id);
        if (knowledge == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }
        return toVO(knowledge, true);
    }

    @Override
    public AiKnowledgeVO toVO(AiKnowledge knowledge, Boolean cached) {
        if (knowledge == null) {
            return null;
        }
        AiKnowledgeVO vo = new AiKnowledgeVO();
        BeanUtils.copyProperties(knowledge, vo);
        vo.setCached(cached);
        return vo;
    }

    private String normalize(String keyword) {
        return keyword.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
