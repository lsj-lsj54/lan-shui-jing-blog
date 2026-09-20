package com.lsj.article.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.article.domain.po.Article;
import com.lsj.article.domain.po.ArticleLike;
import com.lsj.article.mapper.ArticleLikeMapper;
import com.lsj.article.mapper.ArticleMapper;
import com.lsj.article.service.IArticleLikeService;
import com.lsj.article.utils.RedisUtils;
import com.lsj.common.enums.CachePrefixEnum;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章点赞服务
 * 并发安全：依赖数据库唯一索引 UNIQUE(article_id, user_id) 兜底，
 * 插入时捕获 DuplicateKeyException 处理重复点赞
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLike> implements IArticleLikeService {

    private final ArticleMapper articleMapper;
    private final RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long articleId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        // 先查是否已点赞
        ArticleLike like = lambdaQuery()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)
                .one();

        boolean isLiked;
        if (like == null) {
            // 未点赞 → 新增点赞记录（依赖唯一索引防并发重复插入）
            ArticleLike newLike = new ArticleLike();
            newLike.setArticleId(articleId);
            newLike.setUserId(userId);
            try {
                save(newLike);
            } catch (DuplicateKeyException e) {
                // 并发场景：另一个请求已插入，视为已点赞，不重复+1
                log.warn("并发点赞，唯一索引冲突: articleId={}, userId={}", articleId, userId);
                isLiked = true;
                Article article = articleMapper.selectById(articleId);
                evictArticleCache(articleId);
                return buildResult(isLiked, article != null ? article.getLikeCount() : 0);
            }
            // 点赞数+1
            articleMapper.incrementLikeCount(articleId);
            isLiked = true;
        } else {
            // 已点赞 → 删除点赞记录
            removeById(like.getId());
            // 点赞数-1（防止负数）
            articleMapper.decrementLikeCount(articleId);
            isLiked = false;
        }

        // 清除文章详情缓存，确保下次读取最新点赞数
        evictArticleCache(articleId);

        // 查询最新点赞数返回
        Article article = articleMapper.selectById(articleId);
        return buildResult(isLiked, article != null ? article.getLikeCount() : 0);
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId)
                .count() > 0;
    }

    /**
     * 清除文章详情缓存
     */
    private void evictArticleCache(Long articleId) {
        try {
            String detailKey = CachePrefixEnum.ARTICLE.buildKey(articleId);
            redisUtils.delete(detailKey);
        } catch (Exception e) {
            log.warn("清除文章详情缓存失败: articleId={}, error={}", articleId, e.getMessage());
        }
    }

    private Map<String, Object> buildResult(boolean isLiked, Integer likeCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }
}
