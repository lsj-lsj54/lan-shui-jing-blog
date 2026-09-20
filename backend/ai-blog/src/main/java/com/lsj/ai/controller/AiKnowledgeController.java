package com.lsj.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lsj.ai.domain.dto.KnowledgeSearchDTO;
import com.lsj.ai.domain.vo.AiKnowledgeVO;
import com.lsj.ai.service.IAiKnowledgeService;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
@Validated
public class AiKnowledgeController {

    private final IAiKnowledgeService aiKnowledgeService;

    /**
     * 按关键词检索知识：库中已有则直接返回，否则调用 DeepSeek 后落库
     */
    @PostMapping("/search")
    public Result<AiKnowledgeVO> search(@Valid @RequestBody KnowledgeSearchDTO dto,
                                        @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId) {
        return Result.success(aiKnowledgeService.searchAndSave(dto, userId));
    }

    @GetMapping("/page")
    public Result<Page<AiKnowledgeVO>> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(aiKnowledgeService.pageKnowledge(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<AiKnowledgeVO> getById(@PathVariable Long id) {
        return Result.success(aiKnowledgeService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiKnowledgeService.removeById(id);
        return Result.success();
    }
}
