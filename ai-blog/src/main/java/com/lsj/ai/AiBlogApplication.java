package com.lsj.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lsj")
@MapperScan("com.lsj.ai.mapper")
public class AiBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiBlogApplication.class, args);
    }
}