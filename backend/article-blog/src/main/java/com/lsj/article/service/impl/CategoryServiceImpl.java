package com.lsj.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.article.domain.dto.CategoryDTO;
import com.lsj.article.domain.po.Article;
import com.lsj.article.domain.po.Category;
import com.lsj.article.domain.vo.CategoryVO;
import com.lsj.article.mapper.CategoryMapper;
import com.lsj.article.service.IArticleService;
import com.lsj.article.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final IArticleService articleService;

    @Override
    public List<CategoryVO> listByUserId(Long userId) {
        List<Category> categories = lambdaQuery()
                .eq(Category::getUserId, userId)
                .orderByDesc(Category::getCreateTime)
                .list();
        return categories.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO addCategory(CategoryDTO dto, Long userId) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setUserId(userId);
        save(category);
        log.info("分类创建成功: categoryId={}, userId={}", category.getId(), userId);
        return toVO(category);
    }

    /**
     * 删除分类：同时逻辑删除该分类下所有文章
     * 使用直接 SQL 批量更新，不需要先查出来再删，避免大列表内存占用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1. 批量逻辑删除该分类下所有文章（直接 update，不需要 select）
        LambdaUpdateWrapper<Article> articleWrapper = new LambdaUpdateWrapper<>();
        articleWrapper.eq(Article::getCategoryId, id)
                .set(Article::getCategoryId, null); // 文章分类置空，或逻辑删除文章
        articleService.update(articleWrapper);

        // 2. 删除分类
        removeById(id);
        log.info("分类删除成功: categoryId={}", id);
    }

    @Override
    public CategoryVO toVO(Category category) {
        if (category == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
