package com.whatever

import com.whatever.config.properties.RedisProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import okio.IOException
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer

private val logger = KotlinLogging.logger {  }

@Profile("test")
@Configuration
class EmbeddedRedisConfig(
    private val redisProperties: RedisProperties,
) {
    private lateinit var redisServer: RedisServer

    @PostConstruct
    fun postConstruct() {
        redisServer = RedisServer(redisProperties.postAsInt)
        try {
            logger.debug { "Starting Embedded Redis" }
            redisServer.start()
        } catch (e: IOException) {
            logger.error(e) { "Fail to starting Embedded Redis" }
        }
    }

    @PreDestroy
    fun preDestroy() {
        try {
            logger.debug { "Stopping Embedded Redis" }
            redisServer.stop()
        } catch (e: Exception) {
            logger.error(e) { "Fail to stopping Embedded Redis" }
        }
    }

}