package com.whatever.domain.auth.repository

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.base.RedisRepository
import com.whatever.util.DateTimeUtil
import com.whatever.util.withoutNano
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
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

    private fun scanAllRefreshToken(userId: Long): Set<String> {
        val key = "${REFRESH_TOKEN_PREFIX}${userId}:*"
        val targetKeys = mutableSetOf<String>()
        val scanOptions = ScanOptions.scanOptions().match(key).count(20).build()

        try {
            redisTemplate.scan(scanOptions).use {
                while (it.hasNext()) {
                    targetKeys.add(it.next())
                }
            }
            logger.debug { "Found ${targetKeys.size} refresh token keys matching the pattern. owner: ${userId}" }
        } catch (e: Exception) {
            logger.error(e) { "Error during scanning refresh tokens for user: ${userId}" }
        }
        return targetKeys.toSet()
    }

    fun deleteAllRefreshToken(userId: Long): Long {
        val targetKeys = scanAllRefreshToken(userId)
        var deletedKeyCnt = 0L
        try {
            if (targetKeys.isNotEmpty()) {
                deletedKeyCnt = redisTemplate.delete(targetKeys)
            }
            logger.debug { "Successfully deleted ${deletedKeyCnt} refresh token. owner: ${userId}" }
        } catch (e: Exception) {
            logger.error(e) { "Error during deleting refresh tokens for user: ${userId}" }
        }
        return deletedKeyCnt
    }

    fun saveJtiToBlacklist(
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

    fun isJtiBlacklisted(jti: String): Boolean {
        val key = "${SIGN_OUT_JTI_PREFIX}${jti}"
        val result = redisTemplate.opsForValue().get(key)
        return result != null
    }

    fun getJtiBlacklistExpirationTime(
        jti: String,
        zoneId: ZoneId = DateTimeUtil.UTC_ZONE_ID,
    ): LocalDateTime? {
        val key = "${SIGN_OUT_JTI_PREFIX}${jti}"
        val remainingTtlSeconds = getRemainingTtlSeconds(key)
        if (remainingTtlSeconds == 0L) {
            return null
        }
        return DateTimeUtil.localNow(zoneId).plusSeconds(remainingTtlSeconds).withoutNano
    }

    companion object {
        const val REFRESH_TOKEN_PREFIX = "token:refresh:"
        const val SIGN_OUT_JTI_PREFIX = "blacklist:jti:"
    }
}