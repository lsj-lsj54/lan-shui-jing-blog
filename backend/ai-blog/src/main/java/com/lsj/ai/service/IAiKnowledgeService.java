package com.lsj.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.ai.domain.dto.KnowledgeSearchDTO;
import com.lsj.ai.domain.po.AiKnowledge;
import com.lsj.ai.domain.vo.AiKnowledgeVO;

public interface IAiKnowledgeService extends IService<AiKnowledge> {

    AiKnowledgeVO searchAndSave(KnowledgeSearchDTO dto, Long userId);

    Page<AiKnowledgeVO> pageKnowledge(Integer pageNum, Integer pageSize, String keyword);

    AiKnowledgeVO getDetail(Long id);

    AiKnowledgeVO toVO(AiKnowledge knowledge, Boolean cached);
}
