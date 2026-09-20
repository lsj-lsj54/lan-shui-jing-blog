package com.lsj.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.lsj")
@EnableDiscoveryClient
@MapperScan("com.lsj.ai.mapper")
public class AiBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiBlogApplication.class, args);
    }
}