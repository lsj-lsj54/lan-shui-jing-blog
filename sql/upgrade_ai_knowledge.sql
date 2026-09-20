-- ============================================================
-- AI 知识表升级脚本（已有 blog_ai 库时执行）
-- mysql -u root -p blog_ai < upgrade_ai_knowledge.sql
-- ============================================================
USE `blog_ai`;

CREATE TABLE IF NOT EXISTS `t_ai_knowledge` (
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
