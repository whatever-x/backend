package com.whatever.domain.auth.service

import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.exception.AuthException
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.RedisUtil
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authService: AuthService,
) {

    @MockitoSpyBean
    private lateinit var redisUtil: RedisUtil

    @MockitoBean
    private lateinit var jwtHelper: JwtHelper

    private lateinit var securityUtilMock: AutoCloseable

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
        securityUtilMock = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()
        reset(redisUtil)
        reset(jwtHelper)
    }

    @DisplayName("유효하지 않은 리프레시 토큰이면 예외가 발생한다.")
    @Test
    fun refresh_WithInvalidRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceToken(accessToken = "accessToken", refreshToken = "invalidRefreshToken")
        val userId = 1L
        `when`(jwtHelper.getUserId(serviceToken.accessToken)).thenReturn(userId)
        `when`(jwtHelper.isValidJwt(serviceToken.refreshToken)).thenReturn(false)

        // when, then
        assertThatThrownBy { authService.refresh(serviceToken) }
            .isInstanceOf(AuthException::class.java)
    }

    @DisplayName("Redis에 저장된 리프레시 토큰과 다르면 예외가 발생한다.")
    @Test
    fun refresh_WithMismatchedRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceToken(accessToken = "accessToken", refreshToken = "refreshToken")
        val userId = 1L
        val storedRefreshToken = "differentRefreshToken"
        `when`(jwtHelper.getUserId(serviceToken.accessToken)).thenReturn(userId)
        `when`(jwtHelper.isValidJwt(serviceToken.refreshToken)).thenReturn(true)
        `when`(redisUtil.getRefreshToken(userId = userId, deviceId = "tempDeviceIds")).thenReturn(storedRefreshToken)

        // when, then
        assertThatThrownBy { authService.refresh(serviceToken) }
            .isInstanceOf(AuthException::class.java)
    }
}
