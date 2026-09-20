package com.lsj.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lsj.ai.domain.dto.AiMessageDTO;
import com.lsj.ai.domain.po.AiMessage;
import com.lsj.ai.domain.vo.AiChatVO;
import com.lsj.ai.domain.vo.AiMessageVO;
import com.lsj.ai.service.IAiMessageService;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai/message")
@RequiredArgsConstructor
public class AiMessageController {

    private final IAiMessageService aiMessageService;

    @GetMapping("/{id}")
    public Result<AiMessageVO> getById(@PathVariable Long id) {
        return Result.success(aiMessageService.toVO(aiMessageService.getById(id)));
    }

    @GetMapping("/user/{userId}")
    public Result<List<AiMessageVO>> getByUserId(@PathVariable Long userId) {
        return Result.success(aiMessageService.listByUserId(userId));
    }

    @GetMapping("/page")
    public Result<Page<AiMessageVO>> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false) Long userId) {
        Page<AiMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(AiMessage::getUserId, userId);
        }
        wrapper.orderByDesc(AiMessage::getCreateTime);
        Page<AiMessage> result = aiMessageService.page(page, wrapper);
        Page<AiMessageVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(aiMessageService::toVO)
                .collect(Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 发送消息并调用 DeepSeek 生成回复
     */
    @PostMapping
    public Result<AiChatVO> add(@Valid @RequestBody AiMessageDTO dto,
                                @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId) {
        return Result.success(aiMessageService.sendMessage(dto, userId));
    }

    @PutMapping
    public Result<Void> update(@RequestBody AiMessage aiMessage) {
        aiMessageService.updateById(aiMessage);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aiMessageService.removeById(id);
        return Result.success();
    }
}
