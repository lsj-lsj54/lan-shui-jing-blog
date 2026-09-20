package com.lsj.article.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.article.domain.po.ArticleVersion;
import com.lsj.article.mapper.ArticleVersionMapper;
import com.lsj.article.service.IArticleVersionService;
import org.springframework.stereotype.Service;

@Service
public class ArticleVersionServiceImpl extends ServiceImpl<ArticleVersionMapper, ArticleVersion> implements IArticleVersionService {
}