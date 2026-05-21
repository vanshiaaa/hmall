package com.example;

import com.api.config.DefaltFeignLevel;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.example.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = "com.api.client", defaultConfiguration = DefaltFeignLevel.class)
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}