package com.lsj.article.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lsj.article.domain.dto.CommentDTO;
import com.lsj.article.domain.vo.CommentVO;
import com.lsj.article.service.ICommentService;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 文章评论控制器
 */
@RestController
@RequestMapping("/article/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    /**
     * 根据文章ID分页查询评论列表
     */
    @GetMapping("/list/{articleId}")
    public Result<Page<CommentVO>> getListByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        Page<CommentVO> page = new Page<>(pageNum, pageSize);
        return Result.success(commentService.pageByArticleId(page, articleId));
    }

    /**
     * 发表评论
     */
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody CommentDTO dto,
                            @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId,
                            @RequestHeader(CommonConstants.HEADER_USERNAME) String username) {
        return Result.success(commentService.addComment(dto, userId, username));
    }

    /**
     * 删除评论（仅本人可删）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId) {
        commentService.deleteComment(id, userId);
        return Result.success();
    }
}
