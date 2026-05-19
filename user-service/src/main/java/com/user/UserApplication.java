package com.user;

import com.api.config.DefaltFeignLevel;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableFeignClients(basePackages = "com.api.client")
@EnableFeignClients(basePackages = "com.api.client", defaultConfiguration = DefaltFeignLevel.class)
@MapperScan("com.user.mapper")
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}