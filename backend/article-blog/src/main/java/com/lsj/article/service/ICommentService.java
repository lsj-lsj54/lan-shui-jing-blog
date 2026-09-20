package com.lsj.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.article.domain.dto.CommentDTO;
import com.lsj.article.domain.po.Comment;
import com.lsj.article.domain.vo.CommentVO;

public interface ICommentService extends IService<Comment> {

    /**
     * 根据文章ID分页查询评论列表
     */
    <E extends IPage<CommentVO>> E pageByArticleId(E page, Long articleId);

    /**
     * 发表评论
     *
     * @param dto      评论 DTO
     * @param userId   用户ID
     * @param username 用户名
     * @return 评论ID
     */
    Long addComment(CommentDTO dto, Long userId, String username);

    /**
     * 删除评论（仅本人可删）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * PO 转 VO
     */
    CommentVO toVO(Comment comment);
}
