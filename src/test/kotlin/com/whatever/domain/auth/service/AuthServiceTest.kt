package com.whatever.domain.auth.service

import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.exception.AuthException
import com.whatever.domain.auth.exception.AuthExceptionCode
import com.whatever.domain.auth.exception.OidcPublicKeyMismatchException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.RedisUtil
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
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
    private val userRepository: UserRepository,
) {
    @MockitoBean
    private lateinit var oidcCacheManager: CacheManager

    @MockitoBean
    private lateinit var oidcHelper: OIDCHelper

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
        userRepository.deleteAllInBatch()
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

    @DisplayName("캐시에 일치하는 oidc 공개키가 없다면 다시 로드 후 정상 흐름으로 동작한다.")
    @Test
    fun signUpOrSignIn_WithExpiredOidcPublicKey() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val exception = OidcPublicKeyMismatchException(AuthExceptionCode.ILLEGAL_KID)
        val user = userRepository.save(User(
            nickname = "testuser",
            platform = LoginPlatform.KAKAO,
            platformUserId = "test-kakao-user-id",
            userStatus = UserStatus.SINGLE,
        ))
        val fakeKakaoIdTokenPayload = KakaoIdTokenPayload(
            iss = "fake-kakao",
            aud = "fake-kakao",
            sub = user.platformUserId,
            iat = 1000000L,
            exp = 1000000L,
            authTime = 1000000L,
        )
        val fakeServiceToken = ServiceToken(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        whenever(oidcHelper.parseKakaoIdToken(any(), any()))
            .thenThrow(exception)  // Oidc PublicKey가 만료되어 불일치 발생
            .thenReturn(fakeKakaoIdTokenPayload)  // 캐싱 후 다시 정상값 반환
        whenever(jwtHelper.createAccessToken(user.id))
            .thenReturn(fakeServiceToken.accessToken)
        whenever(jwtHelper.createRefreshToken())
            .thenReturn(fakeServiceToken.refreshToken)
        whenever(oidcCacheManager.getCache(oidcPublicKeyCacheName))
            .thenReturn(mock(Cache::class.java))


        // when
        val result = authService.signUpOrSignIn(
            loginPlatform = user.platform,
            idToken = idToken,
        )

        // then
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName))!!.evictIfPresent(user.platform.name)
        assertThat(result.nickname).isEqualTo(user.nickname)
        assertThat(result.serviceToken.accessToken).isEqualTo(fakeServiceToken.accessToken)
        assertThat(result.serviceToken.refreshToken).isEqualTo(fakeServiceToken.refreshToken)
    }
}
