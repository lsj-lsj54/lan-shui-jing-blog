package com.lsj.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lsj.article.domain.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    /**
     * 浏览量原子+1
     */
    @Update("UPDATE t_article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    /**
     * 浏览量原子+N（用于 Redis 计数批量落库）
     */
    @Update("UPDATE t_article SET view_count = view_count + #{count} WHERE id = #{id}")
    void incrementViewCountBy(@Param("id") Long id, @Param("count") Long count);

    /**
     * 点赞数原子+1
     */
    @Update("UPDATE t_article SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    /**
     * 点赞数原子-1（防止负数）
     */
    @Update("UPDATE t_article SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(Long id);
}
