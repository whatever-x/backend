package com.whatever.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.Duration


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

    @Test
    @DisplayName("refresh token을 저장한다.")
    fun saveRefreshToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val expectedKey = "refresh_token:${userId}:${deviceId}"
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
        val expectedKey = "refresh_token:${userId}:${deviceId}"

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
