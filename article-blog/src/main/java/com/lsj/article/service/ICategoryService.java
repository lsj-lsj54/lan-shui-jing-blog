package com.lsj.article.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.article.domain.dto.CategoryDTO;
import com.lsj.article.domain.po.Category;
import com.lsj.article.domain.vo.CategoryVO;

import java.util.List;

public interface ICategoryService extends IService<Category> {

    /**
     * 根据用户ID查询分类列表
     */
    List<CategoryVO> listByUserId(Long userId);

    /**
     * 新增分类
     */
    CategoryVO addCategory(CategoryDTO dto, Long userId);

    /**
     * 删除分类（同时逻辑删除该分类下所有文章）
     */
    void deleteCategory(Long id);

    /**
     * PO 转 VO
     */
    CategoryVO toVO(Category category);
}
