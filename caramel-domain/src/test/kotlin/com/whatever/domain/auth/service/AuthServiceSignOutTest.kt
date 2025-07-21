package com.whatever.domain.auth.service

import com.whatever.caramel.common.global.jwt.JwtHelper
import com.whatever.caramel.common.global.jwt.JwtProperties
import com.whatever.caramel.common.global.jwt.exception.JwtExceptionCode
import com.whatever.caramel.common.global.jwt.exception.JwtMalformedException
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.withoutNano
import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import io.jsonwebtoken.ExpiredJwtException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceSignOutTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authService: AuthService,
    private val jwtProperties: JwtProperties,
) {

    @MockitoSpyBean
    private lateinit var jwtHelper: JwtHelper

    @MockitoSpyBean
    private lateinit var authRedisRepository: AuthRedisRepository

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
    }

    @AfterEach
    fun tearDown() {
    }

    @DisplayName("로그아웃 시 access token은 블랙리스트에 등록하고, refresh token은 재거된다.")
    @Test
    fun signOut() {
        // given
        val user = User(
            id = 0L,
            platform = LoginPlatform.TEST,
            platformUserId = "test-pid"
        )
        val accessToken = jwtHelper.createAccessToken(user.id)
        val refreshToken = jwtHelper.createRefreshToken()
        val deviceId = "test-device"

        val testStartInstant = DateTimeUtil.zonedNow().withoutNano.toInstant()
        authRedisRepository.saveRefreshToken(
            userId = user.id,
            deviceId = deviceId,
            refreshToken = refreshToken,
            ttlSeconds = 300L
        )

        // when
        authService.signOut(
            bearerAccessToken = JwtHelper.BEARER_TYPE + accessToken,
            deviceId = deviceId,
            user.id,
        )

        // then
        val refreshTokenAfterSignOut = authRedisRepository.getRefreshToken(user.id, deviceId)
        assertThat(refreshTokenAfterSignOut).isNull()  // redis에 refresh token은 제거되어야 한다.

        val jti = jwtHelper.extractJti(accessToken)
        val isBlacklisted = authRedisRepository.isJtiBlacklisted(jti)
        assertThat(isBlacklisted).isTrue  // jti는 블랙리스트에 등록되어야 한다.

        val expirationDateTime = authRedisRepository.getJtiBlacklistExpirationTime(jti)
        require(expirationDateTime != null)

        val actualExpireInstant = expirationDateTime.toInstant(ZoneOffset.UTC)
        val expectedExpireInstant = testStartInstant.plusSeconds(jwtProperties.accessExpirationSec)

        assertThat(actualExpireInstant)  // 블랙리스트 만료 시간은 access token 만료시간과 1초 내로 차이가 나야한다.
            .isCloseTo(
                expectedExpireInstant,
                within(1, ChronoUnit.SECONDS)
            )
    }

    @DisplayName("만료된 access token인 경우에는 refresh token만 삭제하고 블랙리스트에 등록하지 않는다.")
    @Test
    fun signOut_WithExpiredAccessToken() {
        // given
        val user = User(
            id = 0L,
            platform = LoginPlatform.TEST,
            platformUserId = "test-pid"
        )
        val accessToken = jwtHelper.createAccessToken(user.id)
        val jti = jwtHelper.extractJti(accessToken)
        val refreshToken = jwtHelper.createRefreshToken()
        val deviceId = "test-device"

        authRedisRepository.saveRefreshToken(
            userId = user.id,
            deviceId = deviceId,
            refreshToken = refreshToken,
            ttlSeconds = 300L
        )

        doThrow(ExpiredJwtException(null, null, "expired"))
            .whenever(jwtHelper).extractJti(any())

        // when
        authService.signOut(
            bearerAccessToken = JwtHelper.BEARER_TYPE + accessToken,
            deviceId = deviceId,
            user.id
        )

        // then
        val refreshTokenAfterSignOut = authRedisRepository.getRefreshToken(user.id, deviceId)
        assertThat(refreshTokenAfterSignOut).isNull()

        val isBlacklist = authRedisRepository.isJtiBlacklisted(jti)
        assertThat(isBlacklist).isFalse
    }

    @DisplayName("잘못된 Bearer 포맷의 헤더를 전달하면 예외가 발생한다.")
    @Test
    fun signOut_WithInvalidBearerFormat() {
        // given
        val user = User(
            id = 0L,
            platform = LoginPlatform.TEST,
            platformUserId = "test-pid"
        )
        val accessTokenWithoutBearer = jwtHelper.createAccessToken(user.id)

        // when
        val result = assertThrows<JwtMalformedException> {
            authService.signOut(
                bearerAccessToken = accessTokenWithoutBearer,
                deviceId = "test-device",
                userId = 0L
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(JwtExceptionCode.PARSE_FAILED)
    }
}

