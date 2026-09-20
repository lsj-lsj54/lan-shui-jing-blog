package com.lsj.article.controller;

import com.lsj.article.domain.dto.CategoryDTO;
import com.lsj.article.domain.vo.CategoryVO;
import com.lsj.article.service.ICategoryService;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章分类控制器
 */
@RestController
@RequestMapping("/article/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 获取当前用户的分类列表
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(
            @RequestHeader(value = CommonConstants.HEADER_USER_ID, required = false) Long userId) {
        if (userId == null) {
            return Result.success(java.util.Collections.emptyList());
        }
        return Result.success(categoryService.listByUserId(userId));
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<CategoryVO> add(@RequestHeader(CommonConstants.HEADER_USER_ID) Long userId,
                                   @Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.addCategory(dto, userId));
    }

    /**
     * 删除分类（同时处理该分类下文章）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
