package com.lsj.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.lsj")
@EnableDiscoveryClient
@MapperScan("com.lsj.article.mapper")
@EnableCaching
@EnableScheduling // 开启定时任务（浏览量批量落库）
public class ArticleBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleBlogApplication.class, args);
    }
}
