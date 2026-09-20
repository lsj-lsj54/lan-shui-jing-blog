package com.lsj.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lsj.article.domain.dto.ArticleDTO;
import com.lsj.article.domain.po.Article;
import com.lsj.article.domain.po.ArticleVersion;
import com.lsj.article.domain.vo.ArticleVO;
import com.lsj.article.service.IArticleService;
import com.lsj.article.service.IArticleVersionService;
import com.lsj.article.service.IArticleLikeService;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final IArticleService articleService;
    private final IArticleVersionService articleVersionService;
    private final IArticleLikeService articleLikeService;

    /**
     * 获取文章详情（浏览量 Redis+1，返回点赞状态）
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getById(
            @PathVariable Long id,
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        return Result.success(articleService.getArticleDetail(id, userId));
    }

    /**
     * 根据用户ID查询文章列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ArticleVO>> getByUserId(@PathVariable Long userId) {
        return Result.success(articleService.listByUserId(userId));
    }

    /**
     * 分页查询文章列表（带缓存）
     */
    @GetMapping("/page")
    public Result<Page<ArticleVO>> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Article::getUserId, userId);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getAuthor, keyword));
        }
        wrapper.orderByDesc(Article::getCreateTime);
        Page<ArticleVO> page = new Page<>(pageNum, pageSize);
        return Result.success(articleService.pageVO(page, wrapper));
    }

    /**
     * 新增文章
     */
    @PostMapping
    public Result<Long> add(@Valid @RequestBody ArticleDTO dto,
                            @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId) {
        return Result.success(articleService.addArticle(dto, userId));
    }

    /**
     * 修改文章（自动保存版本）
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(dto);
        return Result.success();
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.removeById(id);
        return Result.success();
    }

    /**
     * 获取文章版本列表
     */
    @GetMapping("/version/list/{articleId}")
    public Result<List<ArticleVersion>> getVersionList(@PathVariable Long articleId) {
        LambdaQueryWrapper<ArticleVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleVersion::getArticleId, articleId)
                .orderByDesc(ArticleVersion::getVersion);
        return Result.success(articleVersionService.list(wrapper));
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/version/rollback/{versionId}")
    public Result<Void> rollback(@PathVariable Long versionId) {
        ArticleVersion version = articleVersionService.getById(versionId);
        if (version == null) {
            return Result.error("版本不存在");
        }
        Article article = new Article();
        article.setId(version.getArticleId());
        article.setTitle(version.getTitle());
        article.setContent(version.getContent());
        articleService.updateById(article);
        return Result.success();
    }

    /**
     * 点赞/取消点赞（切换状态）
     */
    @PostMapping("/like/{articleId}")
    public Result<Map<String, Object>> toggleLike(
            @PathVariable Long articleId,
            @RequestHeader(CommonConstants.HEADER_USER_ID) Long userId) {
        return Result.success(articleLikeService.toggleLike(articleId, userId));
    }
}
