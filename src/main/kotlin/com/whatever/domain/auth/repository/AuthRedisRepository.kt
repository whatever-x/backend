package com.whatever.domain.auth.repository

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.service.logger
import com.whatever.domain.base.RedisRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.concurrent.TimeUnit

val logger = KotlinLogging.logger {  }

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

    fun saveSignOutJti(
        jti: String,
        expirationDuration: Duration,
    ): Boolean {
        if (expirationDuration.isNegative || expirationDuration.isZero) {
            logger.debug { "Jti not added to blacklist. Already expired. - Jti: [$jti], ExpirationDuration: $expirationDuration" }
            return false
        }

        val key = "${SIGN_OUT_JTI_PREFIX}${jti}"
        redisTemplate.opsForValue().set(key, "1", expirationDuration)
        logger.debug { "Jti added to blacklist - Jti: [$jti], ExpirationDuration: $expirationDuration" }
        return true
    }

    companion object {
        const val REFRESH_TOKEN_PREFIX = "token:refresh:"
        const val SIGN_OUT_JTI_PREFIX = "blacklist:jti:"
    }
}