package com.lsj.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.lsj.article.domain.po.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
