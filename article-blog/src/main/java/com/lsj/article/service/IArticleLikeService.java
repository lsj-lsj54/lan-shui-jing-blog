package com.lsj.article.service;


import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.article.domain.po.ArticleLike;

import java.util.Map;

public interface IArticleLikeService extends IService<ArticleLike> {

    /**
     * 切换点赞状态：已点赞则取消，未点赞则点赞
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return isLiked-是否点赞 likeCount-最新点赞数
     */
    Map<String, Object> toggleLike(Long articleId, Long userId);

    /**
     * 查询用户是否已点赞该文章
     */
    boolean isLiked(Long articleId, Long userId);
}
