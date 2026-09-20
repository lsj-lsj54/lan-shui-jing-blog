-- ============================================================
-- 蓝水晶 BLOG 数据库初始化脚本
-- 包含：blog_user / blog_article / blog_ai 三个库的全部建表语句
-- 字符集：utf8mb4    引擎：InnoDB
-- 执行方式：mysql -u root -p < init_schema.sql
-- ============================================================

-- ============================================================
-- 一、用户库 blog_user
-- ============================================================
CREATE DATABASE IF NOT EXISTS `blog_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog_user`;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID（雪花算法）',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `intro`       VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- ============================================================
-- 二、文章库 blog_article
-- ============================================================
CREATE DATABASE IF NOT EXISTS `blog_article` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog_article`;

-- ----------------------------
-- 文章表
-- ----------------------------
DROP TABLE IF EXISTS `t_article`;
CREATE TABLE `t_article` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`        VARCHAR(100) NOT NULL COMMENT '文章标题',
    `content`      LONGTEXT     NOT NULL COMMENT '文章内容（Markdown）',
    `tags`         VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `summary`      VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `cover_image`  VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `user_id`      BIGINT       DEFAULT NULL COMMENT '作者用户ID',
    `category_id`  BIGINT       DEFAULT NULL COMMENT '分类ID',
    `status`       TINYINT      DEFAULT 1 COMMENT '状态：0-草稿，1-已发布',
    `view_count`   INT          DEFAULT 0 COMMENT '浏览量',
    `like_count`   INT          DEFAULT 0 COMMENT '点赞数',
    `comment_count`INT          DEFAULT 0 COMMENT '评论数',
    `deleted`      TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `author`       VARCHAR(50)  DEFAULT NULL COMMENT '作者名称（冗余）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_create` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ----------------------------
-- 文章点赞表
-- ----------------------------
DROP TABLE IF EXISTS `t_article_like`;
CREATE TABLE `t_article_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  BIGINT   NOT NULL COMMENT '文章ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`) COMMENT '防并发重复点赞',
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- ----------------------------
-- 文章版本表
-- ----------------------------
DROP TABLE IF EXISTS `t_article_version`;
CREATE TABLE `t_article_version` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  BIGINT       NOT NULL COMMENT '文章ID',
    `title`       VARCHAR(100) NOT NULL COMMENT '版本标题',
    `content`     LONGTEXT     NOT NULL COMMENT '版本内容（Markdown）',
    `version`     INT          NOT NULL COMMENT '版本号',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_version` (`article_id`, `version`) COMMENT '防版本号重复',
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章版本表';

-- ----------------------------
-- 分类表
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `user_id`     BIGINT      DEFAULT NULL COMMENT '所属用户ID',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  BIGINT       NOT NULL COMMENT '文章ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论用户ID',
    `username`    VARCHAR(50)  DEFAULT NULL COMMENT '评论用户名（冗余）',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章评论表';


-- ============================================================
-- 三、AI 对话库 blog_ai
-- ============================================================
CREATE DATABASE IF NOT EXISTS `blog_ai` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog_ai`;

-- ----------------------------
-- AI 消息表
-- ----------------------------
DROP TABLE IF EXISTS `t_ai_message`;
CREATE TABLE `t_ai_message` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `role`        VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-AI助手',
    `content`     TEXT     NOT NULL COMMENT '消息内容',
    `deleted`     TINYINT  DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_create` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';

-- ----------------------------
-- AI 知识表（关键词检索结果沉淀）
-- ----------------------------
DROP TABLE IF EXISTS `t_ai_knowledge`;
CREATE TABLE `t_ai_knowledge` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT       NOT NULL COMMENT '最近一次生成/刷新的用户ID',
    `keyword`      VARCHAR(200) NOT NULL COMMENT '规范化关键词（小写、压缩空白）',
    `keyword_raw`  VARCHAR(200) NOT NULL COMMENT '用户原始关键词',
    `title`        VARCHAR(200) DEFAULT NULL COMMENT '知识标题',
    `summary`      VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `content`      LONGTEXT     NOT NULL COMMENT '知识正文（Markdown）',
    `model`        VARCHAR(50)  DEFAULT NULL COMMENT '生成所用模型',
    `deleted`      TINYINT      DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI知识沉淀表';


-- ============================================================
-- 四、初始数据（可选）
-- ============================================================
-- 默认管理员账号（密码：admin123，BCrypt加密）
-- 注意：如果是全新部署，可以取消下面注释创建默认管理员
-- USE `blog_user`;
-- INSERT INTO `t_user` (`id`, `username`, `password`, `nickname`, `status`)
-- VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1);

-- ============================================================
-- 建表完成
-- ============================================================
