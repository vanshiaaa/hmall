package com.api.config;

import org.springframework.context.annotation.Bean;

public class DefaltFeignLevel {
    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}
