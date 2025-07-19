package com.whatever.config.openfeign

import feign.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientGlobalConfig {

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }
}
