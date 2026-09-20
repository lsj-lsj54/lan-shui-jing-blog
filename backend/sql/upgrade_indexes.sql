-- ============================================================
-- 博客系统数据库升级脚本
-- 功能：添加唯一索引（防并发）、查询优化索引
-- 注意：执行前请先备份数据库！
--       若已有重复数据，唯一索引会添加失败，请先清理重复数据
-- ============================================================

-- ========== 用户库 blog_user ==========
-- 用户名唯一索引（防止重复注册）
ALTER TABLE t_user ADD UNIQUE INDEX uk_username (username);

-- ========== 文章库 blog_article ==========

-- 1. 点赞表唯一索引（防并发重复点赞，核心！）
--    注意：执行前需先清理 t_article_like 中已有的重复记录
--    清理重复数据 SQL（保留 id 最小的记录）：
--    DELETE a FROM t_article_like a
--    INNER JOIN t_article_like b
--    ON a.article_id = b.article_id AND a.user_id = b.user_id AND a.id > b.id;
ALTER TABLE t_article_like ADD UNIQUE INDEX uk_article_user (article_id, user_id);

-- 2. 文章版本表唯一索引（防版本号重复）
ALTER TABLE t_article_version ADD UNIQUE INDEX uk_article_version (article_id, version);

-- 3. 文章表查询优化索引
-- 按用户查询文章
ALTER TABLE t_article ADD INDEX idx_user_id (user_id);
-- 按分类查询文章
ALTER TABLE t_article ADD INDEX idx_category_id (category_id);
-- 按创建时间排序（分页查询常用）
ALTER TABLE t_article ADD INDEX idx_create_time (create_time);
-- 复合索引：用户+创建时间（用户文章列表）
ALTER TABLE t_article ADD INDEX idx_user_create (user_id, create_time);

-- 4. 评论表查询优化索引
ALTER TABLE t_comment ADD INDEX idx_article_id (article_id);
ALTER TABLE t_comment ADD INDEX idx_user_id (user_id);

-- 5. 分类表索引
ALTER TABLE t_category ADD INDEX idx_user_id (user_id);

-- ========== AI 库 blog_ai ==========
ALTER TABLE t_ai_message ADD INDEX idx_user_id (user_id);
ALTER TABLE t_ai_message ADD INDEX idx_user_create (user_id, create_time);

-- ============================================================
-- 执行完成后验证：
-- SHOW INDEX FROM t_article_like;
-- SHOW INDEX FROM t_user;
-- ============================================================
