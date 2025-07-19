package com.whatever.domain.couple.repository

import com.whatever.domain.base.RedisRepository
import com.whatever.util.DateTimeUtil
import com.whatever.util.withoutNano
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class InvitationCodeRedisRepository(
    redisTemplate: RedisTemplate<String, String>,
) : RedisRepository(redisTemplate) {

    fun deleteInvitationCode(
        invitationCode: String,
        hostUserId: Long,
    ): Boolean {
        val invitationKey = "${INVITATION_CODE_PREFIX}${invitationCode}"
        val userKey = "${INVITATION_USER_PREFIX}${hostUserId}"
        val userDeleteResult = redisTemplate.delete(invitationKey)
        val keyDeleteResult = redisTemplate.delete(userKey)
        return userDeleteResult && keyDeleteResult
    }

    fun saveInvitationCode(
        userId: Long,
        invitationCode: String,
        expirationTime: Duration = Duration.ofDays(1),
    ): Boolean {
        val invitationKey = "${INVITATION_CODE_PREFIX}${invitationCode}"
        val userKey = "${INVITATION_USER_PREFIX}${userId}"

        val userSaveResult = redisTemplate.opsForValue().setIfAbsent(invitationKey, "$userId", expirationTime) ?: false
        val keySaveResult = redisTemplate.opsForValue().setIfAbsent(userKey, invitationCode, expirationTime) ?: false
        return userSaveResult && keySaveResult
    }

    fun getInvitationCode(userId: Long): String? {
        val userKey = "${INVITATION_USER_PREFIX}${userId}"
        return redisTemplate.opsForValue().get(userKey)
    }

    fun getInvitationUser(invitationCode: String): Long? {
        val invitationKey = "${INVITATION_CODE_PREFIX}${invitationCode}"
        return redisTemplate.opsForValue().get(invitationKey)?.toLong()
    }

    fun getInvitationExpirationTime(
        invitationCode: String,
        zoneId: ZoneId = DateTimeUtil.UTC_ZONE_ID,
    ): LocalDateTime? {
        val invitationKey = "${INVITATION_CODE_PREFIX}${invitationCode}"
        val remainingTtlSeconds = getRemainingTtlSeconds(invitationKey)
        if (remainingTtlSeconds == 0L) {
            return null
        }
        return DateTimeUtil.localNow(zoneId).plusSeconds(remainingTtlSeconds).withoutNano
    }

    companion object {
        private const val INVITATION_CODE_PREFIX = "couple:invitation:code"
        private const val INVITATION_USER_PREFIX = "couple:invitation:user"
    }
}
