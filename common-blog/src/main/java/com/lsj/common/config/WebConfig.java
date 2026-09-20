package com.lsj.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 全局 Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 配置 Web 接口 JSON 序列化：
     * 1. 支持 Java 8 时间类型，统一日期格式
     * 2. Long 类型序列化为 String，避免前端 JS 大整数精度丢失（雪花算法 id 超过 JS 安全整数范围）
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper webObjectMapper = new ObjectMapper();

        // 注册时间类型模块
        webObjectMapper.registerModule(new JavaTimeModule());
        // 禁用时间戳数组格式，统一字符串
        webObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        webObjectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 关键：Long 类型序列化为 String，解决前端 JS 大整数精度丢失问题
        // 雪花算法生成的 id 是 19 位数字（约 2e18），超过 JS Number.MAX_SAFE_INTEGER（约 9e15）
        // 如果直接返回数字，前端 JSON.parse 会丢失精度，导致 id 末尾被四舍五入
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        webObjectMapper.registerModule(longModule);

        converter.setObjectMapper(webObjectMapper);
        // 放到最前面，优先生效
        converters.add(0, converter);
    }
}
