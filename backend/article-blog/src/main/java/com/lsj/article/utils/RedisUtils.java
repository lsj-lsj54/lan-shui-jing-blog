package com.lsj.article.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== String 操作 ==========
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean expire(String key, long time, TimeUnit unit) {
        return redisTemplate.expire(key, time, unit);
    }

    /**
     * 原子递增（用于浏览量、点赞数等计数）
     *
     * @param key   键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ========== 按前缀批量删除（使用 SCAN 替代 KEYS，避免阻塞） ==========
    /**
     * 删除指定前缀下的所有 key
     * 使用 SCAN 迭代遍历，避免 KEYS 命令在大数据量下阻塞 Redis 主线程
     *
     * @param prefix 键前缀，例如 "article:page:"
     * @return 删除的 key 数量
     */
    public Long deleteByPrefix(String prefix) {
        Set<String> keys = scanKeys(prefix + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 使用 SCAN 命令迭代匹配 key
     *
     * @param pattern 匹配模式
     * @return 匹配的 key 集合
     */
    public Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new java.util.HashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                // 游标关闭异常忽略
            }
            return keys;
        });
    }

    // ========== 分布式锁（Lua 脚本保证原子性） ==========
    /**
     * 尝试获取分布式锁
     *
     * @param lockKey       锁 key
     * @param requestId     请求标识（解锁时校验，防止误删他人锁）
     * @param expireSeconds 过期时间（秒）
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(String lockKey, String requestId, long expireSeconds) {
        Boolean flag = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, expireSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放分布式锁
     * 使用 Lua 脚本保证「校验 requestId + 删除锁」的原子性
     *
     * @param lockKey   锁 key
     * @param requestId 请求标识
     * @return true-释放成功，false-释放失败（锁不存在或不属于自己）
     */
    public boolean unlock(String lockKey, String requestId) {
        // Lua 脚本：先校验值是否匹配，匹配才删除，保证原子性
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else return 0 end";
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] keyBytes = lockKey.getBytes(StandardCharsets.UTF_8);
            byte[] valueBytes = requestId.getBytes(StandardCharsets.UTF_8);
            Object evalResult = connection.eval(
                    luaScript.getBytes(StandardCharsets.UTF_8),
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    keyBytes,
                    valueBytes
            );
            return evalResult == null ? 0L : Long.valueOf(evalResult.toString());
        });
        return result != null && result == 1L;
    }
}
