package com.lsj.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lsj.article.domain.po.ArticleVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleVersionMapper extends BaseMapper<ArticleVersion> {
}