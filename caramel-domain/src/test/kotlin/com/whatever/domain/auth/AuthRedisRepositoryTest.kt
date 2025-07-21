package com.whatever.domain.auth

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.domain.auth.repository.AuthRedisRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

@CaramelDomainSpringBootTest
class AuthRedisRepositoryTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authRedisRepository: AuthRedisRepository,
) {

    @Test
    @DisplayName("refresh token을 저장한다.")
    fun saveRefreshToken() {
        // given
        val userId = 1L
        val deviceId = "test-device"
        val expectedKey = "token:refresh:${userId}:${deviceId}"
        val refreshToken = "test.refresh.token"

        // when
        authRedisRepository.saveRefreshToken(userId, deviceId, refreshToken)

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
        authRedisRepository.saveRefreshToken(userId, deviceId, oldRefreshToken)

        // when
        authRedisRepository.saveRefreshToken(userId, deviceId, newRefreshToken)

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
        authRedisRepository.saveRefreshToken(userId, deviceId, refreshToken)

        // when
        val savedToken = authRedisRepository.getRefreshToken(userId, deviceId)

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
        authRedisRepository.saveRefreshToken(userId, deviceId, refreshToken, ttlSeconds)

        // when
        Thread.sleep(Duration.ofMillis(1200))
        val retrievedToken = authRedisRepository.getRefreshToken(userId, deviceId)

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
        authRedisRepository.saveRefreshToken(userId, deviceId, refreshToken, ttlSeconds)

        // when
        authRedisRepository.deleteRefreshToken(userId, deviceId)

        // then
        val savedToken = authRedisRepository.getRefreshToken(userId, deviceId)
        assertThat(savedToken).isNull()
    }
}
