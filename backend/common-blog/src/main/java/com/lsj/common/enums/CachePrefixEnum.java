package com.lsj.common.enums;

/**
 * 缓存键前缀枚举
 * 用于统一管理各业务模块的Redis缓存前缀，规范key命名
 */
public enum CachePrefixEnum {

    // ========== 内容模块 ==========
    ARTICLE("article", "文章缓存"),
    CATEGORY("category", "分类缓存"),
    TAG("tag", "标签缓存"),
    COMMENT("comment", "评论缓存"),

    // ========== 用户模块 ==========
    USER("user", "用户信息缓存"),
    USER_TOKEN("user:token", "用户登录令牌"),

    // ========== 系统辅助 ==========
    CAPTCHA("captcha", "验证码缓存"),
    SMS_CODE("sms:code", "短信验证码"),
    SYSTEM_CONFIG("system:config", "系统配置缓存"),
    LOCK("distributed:lock", "分布式锁前缀");

    /**
     * 缓存键前缀
     */
    private final String prefix;

    /**
     * 业务描述
     */
    private final String description;

    CachePrefixEnum(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }

    /**
     * 拼接完整缓存键
     * 示例：ARTICLE.buildKey(123) → "article:123"
     *      ARTICLE.buildKey("page", 1, 10) → "article:page:1:10"
     */
    public String buildKey(Object... subKeys) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object key : subKeys) {
            sb.append(":").append(key);
        }
        return sb.toString();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
    }
}
