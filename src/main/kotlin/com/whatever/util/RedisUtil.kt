package com.whatever.util

import com.whatever.config.properties.JwtProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisUtil(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties,
) {

    companion object {
        private const val REFRESH_TOKEN_PREFIX = "token:refresh:"
    }

    fun saveRefreshToken(
        userId: Long,
        deviceId: String,
        refreshToken: String,
        ttlSeconds: Long = jwtProperties.refreshExpirationSec,
    ) {
        redisTemplate.opsForValue().set(
            "$REFRESH_TOKEN_PREFIX${userId}:${deviceId}",
            refreshToken,
            ttlSeconds,
            TimeUnit.SECONDS,
        )
    }

    fun getRefreshToken(
        userId: Long,
        deviceId: String,
    ): String? {
        val key = "$REFRESH_TOKEN_PREFIX${userId}:${deviceId}"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteRefreshToken(
        userId: Long,
        deviceId: String,
    ): Boolean {
        val key = "$REFRESH_TOKEN_PREFIX${userId}:${deviceId}"
        return redisTemplate.delete(key)
    }
}