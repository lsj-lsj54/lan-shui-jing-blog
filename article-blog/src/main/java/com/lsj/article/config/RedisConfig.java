package com.lsj.article.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * Redis 序列化与缓存配置类
 * 核心设计：Redis 专用序列化器与 Web 全局序列化器完全隔离
 * 避免 Redis 的多态类型前缀污染 HTTP 接口返回格式，导致前端解析失败
 */
@Configuration
public class RedisConfig {

    /**
     * 构建 Redis 专用的 Jackson 对象映射器
     * 注意：不注册为 Spring Bean，仅内部使用，避免被 Spring MVC 当成全局 JSON 序列化器
     */
    private ObjectMapper buildRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置字段可见性：允许访问所有字段（包括 private），保证序列化时不会遗漏属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 开启默认类型映射：序列化时写入完整类名，反序列化时能精准还原为原 Java 对象
        // 是 Redis 缓存对象必须开启的配置，避免反序列化为 LinkedHashMap 导致强转报错
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // 注册 Java 8 时间类型序列化模块，解决 LocalDateTime/LocalDate 等类型序列化异常
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用日期序列化为时间戳数组，统一使用可读的字符串格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 统一日期时间字符串格式，保证序列化与反序列化格式一致
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return objectMapper;
    }

    /**
     * 配置 RedisTemplate 核心操作模板
     * 所有 Redis 读写操作最终都通过此模板执行
     * @param factory Redis 连接工厂，Spring 自动读取配置文件注入
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 Redis 连接工厂
        template.setConnectionFactory(factory);

        // 创建 JSON 序列化器，使用 Redis 专用的 ObjectMapper 配置
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(buildRedisObjectMapper());

        // 创建字符串序列化器，用于 key 的序列化，保证 Redis key 为纯字符串、可读性好
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 设置普通 key 的序列化方式
        template.setKeySerializer(stringSerializer);
        // 设置 Hash 结构 key 的序列化方式
        template.setHashKeySerializer(stringSerializer);
        // 设置普通 value 的序列化方式
        template.setValueSerializer(jsonSerializer);
        // 设置 Hash 结构 value 的序列化方式
        template.setHashValueSerializer(jsonSerializer);

        // 应用所有配置，完成模板初始化
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 Spring Cache 注解的缓存规则
     * 供 @Cacheable、@CacheEvict 等注解使用，序列化规则与 RedisTemplate 保持一致
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        // 创建与 RedisTemplate 同规则的 JSON 序列化器
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(buildRedisObjectMapper());

        return RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存默认过期时间为 1 小时，避免永久残留脏数据
                .entryTtl(Duration.ofHours(1))
                // 配置缓存 key 的序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // 配置缓存 value 的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));
    }
}
