package com.lsj.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.article.domain.dto.CommentDTO;
import com.lsj.article.domain.po.Comment;
import com.lsj.article.domain.vo.CommentVO;
import com.lsj.article.mapper.CommentMapper;
import com.lsj.article.service.ICommentService;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Override
    @SuppressWarnings("unchecked")
    public <E extends IPage<CommentVO>> E pageByArticleId(E page, Long articleId) {
        IPage<Comment> commentPage = lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime)
                .page(new Page<>(page.getCurrent(), page.getSize()));

        Page<CommentVO> voPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        voPage.setRecords(commentPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));
        return (E) voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentDTO dto, Long userId, String username) {
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setContent(dto.getContent());
        save(comment);
        log.info("评论发表成功: commentId={}, articleId={}, userId={}", comment.getId(), dto.getArticleId(), userId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 校验是否为本人评论
        Comment comment = lambdaQuery()
                .eq(Comment::getId, commentId)
                .eq(Comment::getUserId, userId)
                .one();
        if (comment == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无权删除该评论");
        }
        removeById(commentId);
        log.info("评论删除成功: commentId={}, userId={}", commentId, userId);
    }

    @Override
    public CommentVO toVO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }
}
