package com.lsj.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.lsj")   // 扫描整个 com.lsj，包括 common 模块
@EnableDiscoveryClient
@MapperScan("com.lsj.user.mapper")
public class UserBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserBlogApplication.class, args);
    }
}