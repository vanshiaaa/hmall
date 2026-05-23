package com.search;

import com.api.config.DefaltFeignLevel;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.api.client", defaultConfiguration = DefaltFeignLevel.class)
@MapperScan("com.search.mapper")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}