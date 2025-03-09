package com.whatever.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.temporal.ChronoUnit


@ActiveProfiles("test")
@SpringBootTest
class RedisUtilTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val redisUtil: RedisUtil,
) {

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
    }

    @DisplayName("초대 코드를 저장하면 관련된 key들이 저장된다.")
    @Test
    fun saveCoupleInvitationCode() {
        // given
        val userId = 1L
        val invitationCode = "TESTCODE123"
        val expiration = Duration.ofDays(1)
        val invitationKey = "couple:invitation:code$invitationCode"
        val userKey = "couple:invitation:user$userId"

        // when
        val result = redisUtil.saveCoupleInvitationCode(userId, invitationCode, expiration)

        // then
        assertThat(result).isTrue()
        val storedUserId = redisTemplate.opsForValue().get(invitationKey)
        val storedInvitationCode = redisTemplate.opsForValue().get(userKey)
        assertThat(storedUserId).isEqualTo(userId.toString())
        assertThat(storedInvitationCode).isEqualTo(invitationCode)
    }

    @DisplayName("저장된 초대 코드를 조회하면 올바른 코드가 반환된다.")
    @Test
    fun getCoupleInvitationCode() {
        // given
        val userId = 2L
        val invitationCode = "INVITECODE456"
        val expiration = Duration.ofDays(1)
        val userKey = "couple:invitation:user$userId"
        redisTemplate.opsForValue().set(userKey, invitationCode, expiration)

        // when
        val retrievedCode = redisUtil.getCoupleInvitationCode(userId)

        // then
        assertThat(retrievedCode).isEqualTo(invitationCode)
    }

    @DisplayName("저장된 초대 코드로 user id를 조회할 수 있다.")
    @Test
    fun getCoupleInvitationUser() {
        // given
        val userId = 3L
        val invitationCode = "CODE789"
        val invitationKey = "couple:invitation:code$invitationCode"
        val expiration = Duration.ofDays(1)
        redisTemplate.opsForValue().set(invitationKey, userId.toString(), expiration)

        // when
        val retrievedUserId = redisUtil.getCoupleInvitationUser(invitationCode)

        // then
        assertThat(retrievedUserId).isEqualTo(userId)
    }

    @DisplayName("초대 코드의 만료 시간 조회 시, 남은 TTL이 올바르게 반영된 만료 시간을 반환한다.")
    @Test
    fun getCoupleInvitationExpirationTime() {
        // given
        val userId = 4L
        val invitationCode = "EXPIRATIONTEST"
        val expirationSec: Long = 10L
        val expiration = Duration.ofSeconds(expirationSec)
        val issueDateTime = DateTimeUtil.localNow()
        redisUtil.saveCoupleInvitationCode(userId, invitationCode, expiration)

        // when
        val result = redisUtil.getCoupleInvitationExpirationTime(invitationCode)

        // then
        assertThat(result).isNotNull()
        assertThat(result).isCloseTo(
            issueDateTime.plusSeconds(expirationSec),
            TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS)
        )
    }


    @DisplayName("만료된 초대 코드의 만료 시간을 조회하면 null을 반환한다.")
    @Test
    fun getCoupleInvitationExpirationTime_WithExpiredToken() {
        // given
        val userId = 5L
        val invitationCode = "EXPIRED_CODE"
        val expirationMillis = 500L
        val expiration = Duration.ofMillis(expirationMillis)
        redisUtil.saveCoupleInvitationCode(userId, invitationCode, expiration)

        // when
        Thread.sleep(expirationMillis)
        val expirationDateTime = redisUtil.getCoupleInvitationExpirationTime(invitationCode)

        // then
        assertThat(expirationDateTime).isNull()
    }

    @Test
    @DisplayName("refresh token을 저장한다.")
    fun saveRefreshToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val expectedKey = "token:refresh:${userId}:${deviceId}"
        val refreshToken = "test.refresh.token"

        // when
        redisUtil.saveRefreshToken(userId, deviceId, refreshToken)

        // then
        val savedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(savedToken).isEqualTo(refreshToken)
    }

    @Test
    @DisplayName("같은 key로 토큰 재저장 시 기존 토큰을 대체한다.")
    fun saveRefreshToken_OverrideExistingToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val expectedKey = "token:refresh:${userId}:${deviceId}"

        val oldRefreshToken = "test.refresh.oldToken"
        val newRefreshToken = "test.refresh.newToken"
        redisUtil.saveRefreshToken(userId, deviceId, oldRefreshToken)

        // when
        redisUtil.saveRefreshToken(userId, deviceId, newRefreshToken)

        // then
        val savedToken = redisTemplate.opsForValue().get(expectedKey)
        assertThat(savedToken).isEqualTo(newRefreshToken)
    }

    @Test
    @DisplayName("저장된 토큰을 조회한다")
    fun getRefreshToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val refreshToken = "test.refresh.token"
        redisUtil.saveRefreshToken(userId, deviceId, refreshToken)

        // when
        val savedToken = redisUtil.getRefreshToken(userId, deviceId)

        // then
        assertThat(savedToken).isEqualTo(refreshToken)
    }

    @DisplayName("만료된 토큰을 조회하면 null을 반환한다.")
    @Test
    fun getRefreshToken_WithExpiredToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val refreshToken = "test.refresh.token"
        val ttlSeconds = 1L
        redisUtil.saveRefreshToken(userId, deviceId, refreshToken, ttlSeconds)

        // when
        Thread.sleep(Duration.ofMillis(1200))
        val retrievedToken = redisUtil.getRefreshToken(userId, deviceId)

        // then
        assertThat(retrievedToken).isNull()
    }

    @DisplayName("토큰이 삭제되면 null을 반환한다.")
    @Test
    fun deleteRefreshToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val refreshToken = "test.refresh.token"
        val ttlSeconds = 1L
        redisUtil.saveRefreshToken(userId, deviceId, refreshToken, ttlSeconds)

        // when
        redisUtil.deleteRefreshToken(userId, deviceId)

        // then
        val savedToken = redisUtil.getRefreshToken(userId, deviceId)
        assertThat(savedToken).isNull()
    }
}
