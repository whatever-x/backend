package com.whatever.caramel.domain.couple.repository

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.caramel.common.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.time.temporal.ChronoUnit

@CaramelDomainSpringBootTest
class InvitationCodeRedisRepositoryTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val inviCodeRedisRepository: InvitationCodeRedisRepository,
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
        val result = inviCodeRedisRepository.saveInvitationCode(userId, invitationCode, expiration)

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
        val retrievedCode = inviCodeRedisRepository.getInvitationCode(userId)

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
        val retrievedUserId = inviCodeRedisRepository.getInvitationUser(invitationCode)

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
        inviCodeRedisRepository.saveInvitationCode(userId, invitationCode, expiration)

        // when
        val result = inviCodeRedisRepository.getInvitationExpirationTime(invitationCode)

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
        inviCodeRedisRepository.saveInvitationCode(userId, invitationCode, expiration)

        // when
        Thread.sleep(expirationMillis)
        val expirationDateTime = inviCodeRedisRepository.getInvitationExpirationTime(invitationCode)

        // then
        assertThat(expirationDateTime).isNull()
    }
}
