package com.whatever.domain.auth.repository

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.base.RedisRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class AuthRedisRepository(
    val jwtProperties: JwtProperties,
    redisTemplate: RedisTemplate<String, String>,
) : RedisRepository(redisTemplate) {

    fun saveRefreshToken(
        userId: Long,
        deviceId: String,
        refreshToken: String,
        ttlSeconds: Long = jwtProperties.refreshExpirationSec,
    ) {
        redisTemplate.opsForValue().set(
            "${REFRESH_TOKEN_PREFIX}${userId}:${deviceId}",
            refreshToken,
            ttlSeconds,
            TimeUnit.SECONDS,
        )
    }

    fun getRefreshToken(
        userId: Long,
        deviceId: String,
    ): String? {
        val key = "${REFRESH_TOKEN_PREFIX}${userId}:${deviceId}"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteRefreshToken(
        userId: Long,
        deviceId: String,
    ): Boolean {
        val key = "${REFRESH_TOKEN_PREFIX}${userId}:${deviceId}"
        return redisTemplate.delete(key)
    }

    companion object {
        const val REFRESH_TOKEN_PREFIX = "token:refresh:"
    }
}