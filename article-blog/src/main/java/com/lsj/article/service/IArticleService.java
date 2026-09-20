package com.lsj.article.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.article.domain.dto.ArticleDTO;
import com.lsj.article.domain.po.Article;
import com.lsj.article.domain.vo.ArticleVO;

import java.util.List;

public interface IArticleService extends IService<Article> {

    /**
     * 获取文章详情（浏览量 Redis 原子+1，返回点赞状态）
     *
     * @param id     文章ID
     * @param userId 当前用户ID（可为空）
     * @return 文章详情 VO
     */
    ArticleVO getArticleDetail(Long id, Long userId);

    /**
     * 新增文章
     *
     * @param dto    文章 DTO
     * @param userId 当前用户ID
     * @return 文章ID
     */
    Long addArticle(ArticleDTO dto, Long userId);

    /**
     * 修改文章（自动保存版本）
     *
     * @param dto 文章 DTO
     */
    void updateArticle(ArticleDTO dto);

    /**
     * 根据用户ID查询文章列表
     */
    List<ArticleVO> listByUserId(Long userId);

    /**
     * 分页查询（带缓存）
     */
    <E extends IPage<ArticleVO>> E pageVO(E page, Wrapper<Article> queryWrapper);

    /**
     * PO 转 VO
     */
    ArticleVO toVO(Article article);

    /**
     * 将 Redis 中的浏览量批量落库到数据库
     * 由定时任务调用
     */
    void flushViewCountToDB();
}
