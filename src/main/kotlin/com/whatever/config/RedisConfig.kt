package com.whatever.config

import com.whatever.config.properties.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig (
    private val redisProperties: RedisProperties,
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(
            redisProperties.host,
            redisProperties.postAsInt
        )

        if (redisProperties.password.isNotBlank()) {
            config.setPassword(redisProperties.password)
        }

        return LettuceConnectionFactory(config)
    }
}