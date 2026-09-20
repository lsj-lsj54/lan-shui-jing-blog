package com.lsj.common.constant;

/**
 * 全局常量定义
 */
public final class CommonConstants {

    private CommonConstants() {
    }

    // ========== 请求头 ==========
    /**
     * 用户ID请求头（网关解析 Token 后注入）
     */
    public static final String HEADER_USER_ID = "user-id";

    /**
     * 用户名请求头
     */
    public static final String HEADER_USERNAME = "username";

    /**
     * Authorization 请求头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    // ========== 默认值 ==========
    /**
     * 默认分页页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小（防止恶意传参导致 OOM）
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 文章摘要默认截取长度
     */
    public static final int ARTICLE_SUMMARY_LENGTH = 200;

    // ========== 状态 ==========
    /**
     * 用户状态：正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态：禁用
     */
    public static final int USER_STATUS_DISABLED = 0;

    // ========== 缓存 ==========
    /**
     * 文章详情缓存过期时间（小时）
     */
    public static final long ARTICLE_DETAIL_CACHE_HOURS = 1;

    /**
     * 文章分页缓存过期时间（分钟）
     */
    public static final long ARTICLE_PAGE_CACHE_MINUTES = 20;

    /**
     * 浏览量 Redis 计数 key 前缀
     */
    public static final String VIEW_COUNT_PREFIX = "article:view:";

    /**
     * 浏览量定时落库批次大小
     */
    public static final int VIEW_COUNT_BATCH_SIZE = 100;
}
