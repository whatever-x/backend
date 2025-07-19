package com.whatever.domain.auth.service

import com.whatever.domain.auth.client.KakaoKapiClient
import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.domain.auth.client.dto.KakaoUnlinkUserResponse
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.exception.AuthException
import com.whatever.domain.auth.exception.AuthExceptionCode
import com.whatever.domain.auth.exception.OidcPublicKeyMismatchException
import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.auth.service.JwtHelper.Companion.BEARER_TYPE
import com.whatever.domain.content.service.createCouple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.couple.service.CoupleService
import com.whatever.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode.UNKNOWN
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.findByIdAndNotDeleted
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
) : ExcludeAsyncConfigBean() {

    @MockitoBean
    private lateinit var oidcCacheManager: CacheManager

    @MockitoBean
    private lateinit var oidcHelper: OIDCHelper

    @MockitoSpyBean
    private lateinit var authRedisRepository: AuthRedisRepository

    @MockitoSpyBean
    private lateinit var jwtHelper: JwtHelper

    @MockitoSpyBean
    private lateinit var coupleService: CoupleService

    @MockitoBean
    private lateinit var kakaoKapiClient: KakaoKapiClient

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
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("유효하지 않은 리프레시 토큰이면 예외가 발생한다.")
    @Test
    fun refresh_WithInvalidRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceToken(accessToken = "accessToken", refreshToken = "invalidRefreshToken")
        val deviceId = "test-device"
        val userId = 1L
        doReturn(userId)
            .whenever(jwtHelper).extractUserIdIgnoringSignature(serviceToken.accessToken)
        doReturn(false)
            .whenever(jwtHelper).isValidJwt(serviceToken.refreshToken)

        // when, then
        assertThatThrownBy { authService.refresh(serviceToken, deviceId) }
            .isInstanceOf(AuthException::class.java)
    }

    @DisplayName("Redis에 저장된 리프레시 토큰과 다르면 예외가 발생한다.")
    @Test
    fun refresh_WithMismatchedRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceToken(accessToken = "accessToken", refreshToken = "refreshToken")
        val deviceId = "test-device"
        val userId = 1L
        val storedRefreshToken = "differentRefreshToken"
        doReturn(userId)
            .whenever(jwtHelper).extractUserIdIgnoringSignature(serviceToken.accessToken)
        doReturn(true)
            .whenever(jwtHelper).isValidJwt(serviceToken.refreshToken)
        `when`(authRedisRepository.getRefreshToken(userId = userId, deviceId = "tempDeviceIds")).thenReturn(
            storedRefreshToken
        )

        // when, then
        assertThatThrownBy { authService.refresh(serviceToken, deviceId) }
            .isInstanceOf(AuthException::class.java)
    }

    @DisplayName("캐시에 일치하는 oidc 공개키가 없다면 다시 로드 후 정상 흐름으로 동작한다.")
    @Test
    fun signUpOrSignIn_WithExpiredOidcPublicKey() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
        val exception = OidcPublicKeyMismatchException(AuthExceptionCode.ILLEGAL_KID)
        val user = userRepository.save(
            User(
                nickname = "testuser",
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-kakao-user-id",
                userStatus = UserStatus.SINGLE,
            )
        )
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

        doReturn(fakeServiceToken.accessToken)
            .whenever(jwtHelper).createAccessToken(user.id)
        doReturn(fakeServiceToken.refreshToken)
            .whenever(jwtHelper).createRefreshToken()
        whenever(oidcCacheManager.getCache(oidcPublicKeyCacheName))
            .thenReturn(mock(Cache::class.java))

        // when
        val result = authService.signUpOrSignIn(
            loginPlatform = user.platform,
            idToken = idToken,
            deviceId = deviceId,
        )

        // then
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName))!!.evictIfPresent(user.platform.name)
        assertThat(result.nickname).isEqualTo(user.nickname)
        assertThat(result.serviceToken.accessToken).isEqualTo(fakeServiceToken.accessToken)
        assertThat(result.serviceToken.refreshToken).isEqualTo(fakeServiceToken.refreshToken)
    }

    @DisplayName("회원 탈퇴 시 유저 상태가 COUPLED라면 커플탈퇴와 유저삭제, 로그아웃을 진행한다.")
    @Test
    fun deleteUser_WithCoupleUser() {
        // given
        val (myUser, partnerUser, couple) = createCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            myPlatformUserId = "123",
        )

        val (accessToken, refreshToken, deviceId) = loginFixture(myUser)

        doReturn(KakaoUnlinkUserResponse(myUser.platformUserId.toLong()))
            .whenever(kakaoKapiClient).unlinkUserByAdminKey(any(), any())

        // when
        authService.deleteUser(
            userId = myUser.id,
            bearerAccessToken = "${BEARER_TYPE}${accessToken}",
            deviceId = deviceId,
        )

        // then
        val oldCouple = coupleRepository.findByIdWithMembers(couple.id)
        require(oldCouple != null)
        assertThat(oldCouple.members.map { it.id })
            .hasSize(1)
            .doesNotContain(myUser.id)

        val oldUser = userRepository.findByIdAndNotDeleted(myUser.id)
        assertThat(oldUser).isNull()

        val jti = jwtHelper.extractJti(accessToken)
        assertThat(authRedisRepository.isJtiBlacklisted(jti)).isTrue
        assertThat(authRedisRepository.getRefreshToken(myUser.id, deviceId)).isNull()
    }

    @DisplayName("회원 탈퇴 시 유저 상태가 SINGLE이라면 유저삭제, 로그아웃을 진행한다.")
    @Test
    fun deleteUser_WithSingleUser() {
        // given
        val myUser = createSingleUser(userRepository)

        val (accessToken, refreshToken, deviceId) = loginFixture(myUser)

        doReturn(KakaoUnlinkUserResponse(myUser.platformUserId.toLong()))
            .whenever(kakaoKapiClient).unlinkUserByAdminKey(any(), any())

        // when
        authService.deleteUser(
            userId = myUser.id,
            bearerAccessToken = "${BEARER_TYPE}${accessToken}",
            deviceId = deviceId,
        )

        // then
        verify(coupleService, never()).leaveCouple(any(), any())

        val oldUser = userRepository.findByIdAndNotDeleted(myUser.id)
        assertThat(oldUser).isNull()

        val jti = jwtHelper.extractJti(accessToken)
        assertThat(authRedisRepository.isJtiBlacklisted(jti)).isTrue
        assertThat(authRedisRepository.getRefreshToken(myUser.id, deviceId)).isNull()
    }

    @DisplayName("회원 탈퇴 시 유저 여러 디바이스에서 로그인을 해도 refresh token을 모두 폐기한다.")
    @Test
    fun deleteUser_WithMultiLoginRefreshToken() {
        // given
        val myUser = createSingleUser(userRepository)

        val (accessToken, refreshToken, deviceId) = loginFixture(myUser)
        (2..4).forEach { i ->
            authRedisRepository.saveRefreshToken(
                userId = myUser.id,
                deviceId = "device$i",
                refreshToken = "refresh-token-$i",
            )
        }

        doReturn(KakaoUnlinkUserResponse(myUser.platformUserId.toLong()))
            .whenever(kakaoKapiClient).unlinkUserByAdminKey(any(), any())

        // when
        authService.deleteUser(
            userId = myUser.id,
            bearerAccessToken = "${BEARER_TYPE}${accessToken}",
            deviceId = deviceId,
        )

        // then
        assertThat(authRedisRepository.deleteAllRefreshToken(myUser.id)).isZero
    }

    @DisplayName("회원 탈퇴 시 예외가 발생하면 롤백되어 유저가 탈퇴되지 않는다.")
    @Test
    fun deleteUser_WithExceptionAndRollBack() {
        // given
        val (myUser, partnerUser, couple) = createCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            myPlatformUserId = "123"
        )
        val (accessToken, refreshToken, deviceId) = loginFixture(myUser)

        doThrow(GlobalException(errorCode = UNKNOWN))
            .whenever(coupleService).leaveCouple(couple.id, myUser.id)

        // when
        assertFailsWith<GlobalException> {
            authService.deleteUser(
                userId = myUser.id,
                bearerAccessToken = "${BEARER_TYPE}$accessToken",
                deviceId = deviceId
            )
        }

        // then
        val existing = userRepository.findByIdAndNotDeleted(myUser.id)
        assertThat(existing).isNotNull

        val savedCouple = coupleRepository.findByIdWithMembers(couple.id)!!
        assertThat(savedCouple.members.map { it.id })
            .containsExactlyInAnyOrder(myUser.id, partnerUser.id)

        val jti = jwtHelper.extractJti(accessToken)
        assertThat(authRedisRepository.isJtiBlacklisted(jti)).isFalse()
        assertThat(authRedisRepository.getRefreshToken(myUser.id, deviceId)).isEqualTo(refreshToken)
    }

    private fun loginFixture(myUser: User): Triple<String, String, String> {
        val accessToken = jwtHelper.createAccessToken(myUser.id)
        val refreshToken = jwtHelper.createRefreshToken()
        val deviceId = "test-device"
        authRedisRepository.saveRefreshToken(
            userId = myUser.id,
            deviceId = deviceId,
            refreshToken = refreshToken,
            ttlSeconds = 300L
        )
        return Triple(accessToken, refreshToken, deviceId)
    }
}

fun createSingleUser(
    userRepository: UserRepository,
    email: String = "test@email.test",
    birthDate: LocalDate = DateTimeUtil.localNow().toLocalDate(),
    nickname: String = "testuser",
    gender: UserGender = UserGender.FEMALE,
    platform: LoginPlatform = LoginPlatform.TEST,
    platformUserId: Long = 1L,
): User {
    return userRepository.save(
        User(
            email = email,
            birthDate = birthDate,
            platform = platform,
            platformUserId = platformUserId.toString(),
            nickname = nickname,
            gender = gender,
            userStatus = UserStatus.SINGLE,
        )
    )
}
