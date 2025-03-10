package com.whatever.util

import com.whatever.config.properties.JwtProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Component
class RedisUtil(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties,
) {

    companion object {
        private const val REFRESH_TOKEN_PREFIX = "token:refresh:"
        private const val INVITATION_CODE_PREFIX = "couple:invitation:code"
        private const val INVITATION_USER_PREFIX = "couple:invitation:user"
    }

    //  TODO(준용): Tx 추가
    fun deleteCoupleInvitationCode(
        invitationCode: String,
        hostUserId: Long,
    ): Boolean {
        val invitationKey = "$INVITATION_CODE_PREFIX${invitationCode}"
        val userKey = "$INVITATION_USER_PREFIX${hostUserId}"
        val userDeleteResult = redisTemplate.delete(invitationKey)
        val keyDeleteResult = redisTemplate.delete(userKey)
        return userDeleteResult && keyDeleteResult
    }

    //  TODO(준용): Tx 추가
    fun saveCoupleInvitationCode(
        userId: Long,
        invitationCode: String,
        expirationTime: Duration = Duration.ofDays(1),
    ): Boolean {
        val invitationKey = "$INVITATION_CODE_PREFIX${invitationCode}"
        val userKey = "$INVITATION_USER_PREFIX${userId}"

        val userSaveResult = redisTemplate.opsForValue().setIfAbsent(invitationKey, "$userId", expirationTime) ?: false
        val keySaveResult = redisTemplate.opsForValue().setIfAbsent(userKey, invitationCode, expirationTime) ?: false
        return userSaveResult && keySaveResult
    }

    fun getCoupleInvitationCode(userId: Long): String? {
        val userKey = "$INVITATION_USER_PREFIX${userId}"
        return redisTemplate.opsForValue().get(userKey)
    }

    fun getCoupleInvitationUser(invitationCode: String): Long? {
        val invitationKey = "$INVITATION_CODE_PREFIX${invitationCode}"
        return redisTemplate.opsForValue().get(invitationKey)?.toLong()
    }

    fun getCoupleInvitationExpirationTime(
        invitationCode: String,
        zoneId: ZoneId = DateTimeUtil.UTC_ZONE_ID,
    ): LocalDateTime? {
        val invitationKey = "$INVITATION_CODE_PREFIX${invitationCode}"
        return redisTemplate.getExpire(invitationKey)
            .takeIf { it >= 0 }
            ?.let { DateTimeUtil.localNow().plusSeconds(it) }
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