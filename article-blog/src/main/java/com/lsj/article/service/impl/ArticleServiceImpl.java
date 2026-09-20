package com.lsj.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.article.domain.dto.ArticleDTO;
import com.lsj.article.domain.po.Article;
import com.lsj.article.domain.po.ArticleVersion;
import com.lsj.article.domain.vo.ArticleVO;
import com.lsj.article.mapper.ArticleMapper;
import com.lsj.article.service.IArticleLikeService;
import com.lsj.article.service.IArticleService;
import com.lsj.article.service.IArticleVersionService;
import com.lsj.article.utils.RedisUtils;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.enums.CachePrefixEnum;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 * 核心优化：
 * 1. 浏览量使用 Redis 计数器（INCR），定时任务批量落库，避免每次访问都写数据库
 * 2. 详情缓存与浏览量解耦，浏览量递增不删除详情缓存
 * 3. 分页缓存使用 SCAN 清理，避免 KEYS 阻塞
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    private final RedisUtils redisUtils;
    private final IArticleVersionService articleVersionService;
    private final IArticleLikeService articleLikeService;

    /**
     * 分页缓存统一前缀
     */
    private static final String PAGE_CACHE_PREFIX = "page";

    /**
     * 服务启动时清除所有分页缓存
     * 修复历史缓存 key 生成 bug 导致的脏数据
     */
    @jakarta.annotation.PostConstruct
    public void clearPageCacheOnStartup() {
        try {
            String pagePrefix = CachePrefixEnum.ARTICLE.buildKey(PAGE_CACHE_PREFIX);
            Long count = redisUtils.deleteByPrefix(pagePrefix);
            log.info("启动时清除文章分页缓存，共清除 {} 个 key", count);
        } catch (Exception e) {
            log.warn("启动时清除文章分页缓存失败（Redis 可能未就绪）: {}", e.getMessage());
        }
    }

    @Override
    public Article getById(Serializable id) {
        String key = CachePrefixEnum.ARTICLE.buildKey(id);
        Object value = redisUtils.get(key);
        if (value != null) {
            return (Article) value;
        }
        // 缓存未命中，查询数据库
        Article article = super.getById(id);
        if (article != null) {
            // 写入缓存并设置过期时间，兜底避免永久脏数据
            redisUtils.set(key, article, CommonConstants.ARTICLE_DETAIL_CACHE_HOURS, TimeUnit.HOURS);
        }
        return article;
    }

    @Override
    public ArticleVO getArticleDetail(Long id, Long userId) {
        // 1. 浏览量 Redis 原子+1（不写数据库，由定时任务批量落库）
        String viewCountKey = CommonConstants.VIEW_COUNT_PREFIX + id;
        redisUtils.increment(viewCountKey, 1);
        // 设置过期时间，防止 key 永久残留（24小时）
        redisUtils.expire(viewCountKey, 24, TimeUnit.HOURS);

        // 2. 查询文章详情（走缓存，浏览量递增不删缓存）
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 3. 组装 VO，注入点赞状态
        ArticleVO vo = toVO(article);
        // 关键：浏览量 = 数据库已落库的 + Redis 中未落库的，实现实时更新
        int redisViewCount = getRedisViewCount(id);
        vo.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + redisViewCount);
        vo.setIsLiked(articleLikeService.isLiked(id, userId));
        return vo;
    }

    /**
     * 获取 Redis 中未落库的浏览量
     */
    private int getRedisViewCount(Long articleId) {
        try {
            String viewCountKey = CommonConstants.VIEW_COUNT_PREFIX + articleId;
            Object value = redisUtils.get(viewCountKey);
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            log.warn("获取Redis浏览量失败: articleId={}, error={}", articleId, e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addArticle(ArticleDTO dto, Long userId) {
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setUserId(userId);
        // 作者从登录用户获取（这里简化，实际应通过 Feign 调用用户服务获取昵称）
        article.setAuthor("用户_" + userId);
        // 摘要为空时自动从内容截取
        if (!StringUtils.hasText(article.getSummary())) {
            String content = article.getContent() == null ? "" : article.getContent();
            article.setSummary(content.length() > CommonConstants.ARTICLE_SUMMARY_LENGTH
                    ? content.substring(0, CommonConstants.ARTICLE_SUMMARY_LENGTH)
                    : content);
        }
        // 初始化计数
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        // 默认已发布
        if (article.getStatus() == null) {
            article.setStatus(1);
        }
        save(article);
        log.info("文章发布成功: articleId={}, userId={}", article.getId(), userId);
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文章ID不能为空");
        }
        Article oldArticle = getById(dto.getId());
        if (oldArticle == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        // 1. 保存旧版本（版本号递增：查最大版本+1，数据库加唯一索引兜底）
        ArticleVersion latestVersion = articleVersionService.lambdaQuery()
                .eq(ArticleVersion::getArticleId, dto.getId())
                .orderByDesc(ArticleVersion::getVersion)
                .last("LIMIT 1")
                .one();
        int nextVersion = (latestVersion == null || latestVersion.getVersion() == null)
                ? 1 : latestVersion.getVersion() + 1;
        ArticleVersion version = new ArticleVersion();
        version.setArticleId(dto.getId());
        version.setTitle(oldArticle.getTitle());
        version.setContent(oldArticle.getContent());
        version.setVersion(nextVersion);
        articleVersionService.save(version);

        // 2. 更新文章
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        // 摘要为空时自动截取
        if (!StringUtils.hasText(article.getSummary())) {
            String content = article.getContent() == null ? "" : article.getContent();
            article.setSummary(content.length() > CommonConstants.ARTICLE_SUMMARY_LENGTH
                    ? content.substring(0, CommonConstants.ARTICLE_SUMMARY_LENGTH)
                    : content);
        }
        updateById(article);
        log.info("文章修改成功: articleId={}, 新版本号={}", dto.getId(), version.getVersion());
    }

    @Override
    public List<ArticleVO> listByUserId(Long userId) {
        List<Article> articles = lambdaQuery()
                .eq(Article::getUserId, userId)
                .orderByDesc(Article::getCreateTime)
                .list();
        return articles.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends IPage<ArticleVO>> E pageVO(E page, Wrapper<Article> queryWrapper) {
        // 1. 基于查询条件生成唯一标识
        String conditionKey = buildConditionKey(queryWrapper);
        // 2. 构建完整分页缓存键
        String cacheKey = CachePrefixEnum.ARTICLE.buildKey(
                PAGE_CACHE_PREFIX,
                page.getCurrent(),
                page.getSize(),
                conditionKey
        );

        // 3. 优先查缓存
        Object cacheValue = redisUtils.get(cacheKey);
        if (cacheValue != null) {
            return (E) cacheValue;
        }

        // 4. 缓存未命中，查数据库
        IPage<Article> articlePage = super.page(new Page<>(page.getCurrent(), page.getSize()), queryWrapper);

        // 5. PO 转 VO
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(articlePage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));

        // 6. 写入缓存
        redisUtils.set(cacheKey, voPage, CommonConstants.ARTICLE_PAGE_CACHE_MINUTES, TimeUnit.MINUTES);
        return (E) voPage;
    }

    @Override
    public ArticleVO toVO(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }

    /**
     * 新增文章：先入库，成功后清空所有分页缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Article entity) {
        boolean success = super.save(entity);
        if (success) {
            clearPageCache();
        }
        return success;
    }

    /**
     * 修改文章：先更新数据库，成功后删除详情缓存 + 清空所有分页缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Article entity) {
        boolean success = super.updateById(entity);
        if (success) {
            String detailKey = CachePrefixEnum.ARTICLE.buildKey(entity.getId());
            redisUtils.delete(detailKey);
            clearPageCache();
        }
        return success;
    }

    /**
     * 删除文章：先删数据库，成功后删除详情缓存 + 清空所有分页缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        boolean success = super.removeById(id);
        if (success) {
            String detailKey = CachePrefixEnum.ARTICLE.buildKey(id);
            redisUtils.delete(detailKey);
            clearPageCache();
        }
        return success;
    }

    /**
     * 将 Redis 中的浏览量批量落库到数据库
     * 由定时任务调用，避免每次访问都写数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void flushViewCountToDB() {
        // 1. SCAN 遍历所有浏览量计数 key
        String pattern = CommonConstants.VIEW_COUNT_PREFIX + "*";
        java.util.Set<String> keys = redisUtils.scanKeys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        log.info("开始批量落库浏览量，共 {} 个 key", keys.size());

        int successCount = 0;
        for (String key : keys) {
            try {
                // 提取文章ID
                String articleIdStr = key.substring(CommonConstants.VIEW_COUNT_PREFIX.length());
                Long articleId = Long.parseLong(articleIdStr);
                // 获取计数值
                Object countObj = redisUtils.get(key);
                if (countObj == null) {
                    continue;
                }
                long count = Long.parseLong(countObj.toString());
                if (count <= 0) {
                    continue;
                }
                // 原子累加数据库
                baseMapper.incrementViewCountBy(articleId, count);
                // 删除已落库的 key
                redisUtils.delete(key);
                successCount++;
            } catch (Exception e) {
                log.error("浏览量落库失败, key={}, error={}", key, e.getMessage());
            }
        }
        log.info("浏览量批量落库完成，成功 {} 个", successCount);
    }

    /**
     * 私有工具：生成查询条件唯一标识
     * 必须包含 SQL 片段 + 实际参数值，否则不同参数的查询会共享缓存导致数据错乱
     */
    private String buildConditionKey(Wrapper<Article> queryWrapper) {
        if (queryWrapper == null) {
            return "none";
        }
        StringBuilder sb = new StringBuilder(queryWrapper.getSqlSegment());
        // 关键：拼接实际参数值，避免不同用户/分类的查询共用同一个缓存 key
        if (queryWrapper instanceof com.baomidou.mybatisplus.core.conditions.AbstractWrapper) {
            java.util.Map<String, Object> paramMap =
                    ((com.baomidou.mybatisplus.core.conditions.AbstractWrapper<?, ?, ?>) queryWrapper)
                            .getParamNameValuePairs();
            if (paramMap != null && !paramMap.isEmpty()) {
                // 按 key 排序后拼接，保证相同参数生成相同 key
                paramMap.keySet().stream().sorted().forEach(key ->
                        sb.append("|").append(key).append("=").append(paramMap.get(key))
                );
            }
        }
        return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 私有工具：清空所有文章分页缓存（使用 SCAN 替代 KEYS）
     */
    private void clearPageCache() {
        String pagePrefix = CachePrefixEnum.ARTICLE.buildKey(PAGE_CACHE_PREFIX);
        redisUtils.deleteByPrefix(pagePrefix);
    }
}
