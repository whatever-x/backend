package com.whatever.caramel.domain.auth.service

import com.whatever.caramel.common.global.exception.GlobalException
import com.whatever.caramel.common.global.exception.GlobalExceptionCode
import com.whatever.caramel.common.global.jwt.JwtHelper
import com.whatever.caramel.common.global.jwt.JwtHelper.Companion.BEARER_TYPE
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.auth.exception.AuthException
import com.whatever.caramel.domain.auth.exception.AuthExceptionCode.ILLEGAL_KID
import com.whatever.caramel.domain.auth.exception.AuthExceptionCode.USER_PROVIDER_NOT_FOUND
import com.whatever.caramel.domain.auth.exception.AuthFailedException
import com.whatever.caramel.domain.auth.exception.IllegalOidcTokenException
import com.whatever.caramel.domain.auth.exception.OidcPublicKeyMismatchException
import com.whatever.caramel.domain.auth.repository.AuthRedisRepository
import com.whatever.caramel.domain.auth.vo.ServiceTokenVo
import com.whatever.caramel.domain.content.service.createCouple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.CoupleService
import com.whatever.caramel.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.caramel.domain.findByIdAndNotDeleted
import com.whatever.caramel.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.caramel.domain.user.exception.UserNotFoundException
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.infrastructure.client.KakaoKapiClient
import com.whatever.caramel.infrastructure.client.KakaoOIDCClient
import com.whatever.caramel.infrastructure.client.dto.KakaoIdTokenPayload
import com.whatever.caramel.infrastructure.client.dto.KakaoUnlinkUserResponse
import com.whatever.caramel.infrastructure.client.dto.OIDCPublicKeysResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

@CaramelDomainSpringBootTest
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

    @MockitoBean
    private lateinit var kakaoOIDCClient: KakaoOIDCClient

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("성공적으로 token refresh에 성공한다.")
    @Test
    fun refresh() {
        // given
        val oldServiceToken = ServiceTokenVo(accessToken = "accessToken", refreshToken = "refreshToken")
        val deviceId = "test-device"
        val userId = 0L
        doReturn(userId)
            .whenever(jwtHelper).extractUserIdIgnoringSignature(oldServiceToken.accessToken)
        doReturn(true)
            .whenever(jwtHelper).isValidJwt(oldServiceToken.refreshToken)
        whenever(authRedisRepository.getRefreshToken(userId = userId, deviceId = deviceId))
            .thenReturn(oldServiceToken.refreshToken)

        // when
        val result = authService.refresh(
            accessToken = oldServiceToken.accessToken,
            refreshToken = oldServiceToken.refreshToken,
            deviceId = deviceId
        )

        // then
        assertThat(result.accessToken).isNotEqualTo(oldServiceToken.accessToken)
        assertThat(result.refreshToken).isNotEqualTo(oldServiceToken.refreshToken)
    }

    @DisplayName("유효하지 않은 리프레시 토큰이면 예외가 발생한다.")
    @Test
    fun refresh_WithInvalidRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceTokenVo(accessToken = "accessToken", refreshToken = "invalidRefreshToken")
        val deviceId = "test-device"
        val userId = 1L
        doReturn(userId)
            .whenever(jwtHelper).extractUserIdIgnoringSignature(serviceToken.accessToken)
        doReturn(false)
            .whenever(jwtHelper).isValidJwt(serviceToken.refreshToken)

        // when, then
        assertThrows<AuthException> {
            authService.refresh(
                accessToken = serviceToken.accessToken,
                refreshToken = serviceToken.refreshToken,
                deviceId = deviceId
            )
        }
    }

    @DisplayName("Redis에 저장된 리프레시 토큰과 다르면 예외가 발생한다.")
    @Test
    fun refresh_WithMismatchedRefreshToken_ThrowsException() {
        // given
        val serviceToken = ServiceTokenVo(accessToken = "accessToken", refreshToken = "refreshToken")
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
        assertThrows<AuthException> {
            authService.refresh(
                accessToken = serviceToken.accessToken,
                refreshToken = serviceToken.refreshToken,
                deviceId = deviceId
            )
        }
    }

    @DisplayName("로그인이 지원하지 않는 플랫폼으로 이뤄지면 예외를 반환한다.")
    @Test
    fun signUpOrSignIn_whenInvalidLoginPlatform_thenThrowException() {
        // given, when
        val result = assertThrows<AuthFailedException> {
            authService.signUpOrSignIn(
                loginPlatform = LoginPlatform.TEST,
                idToken = "test-id-token",
                deviceId = "test-device-id",
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(USER_PROVIDER_NOT_FOUND)
    }

    @DisplayName("로그인 중 OidcPublicKeyMismatchException이 아닌 예외가 반환되면, 재시도 하지 않고 AuthFailedException을 반환한다.")
    @Test
    fun signUpOrSignIn_whenUnexpectedException_thenThrowException() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
        val user = userRepository.save(
            User(
                nickname = "testuser",
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-kakao-user-id",
                userStatus = UserStatus.SINGLE,
            )
        )
        whenever(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())
        whenever(oidcHelper.parseKakaoIdToken(any(), any()))
            .thenThrow(RuntimeException("Unexpected Exception"))
        whenever(oidcCacheManager.getCache(oidcPublicKeyCacheName))
            .thenReturn(mock(Cache::class.java))

        // when, then
        assertThrows<AuthFailedException> {
            authService.signUpOrSignIn(
                loginPlatform = user.platform,
                idToken = idToken,
                deviceId = deviceId,
            )
        }
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName), never())!!.evictIfPresent(any())
    }

    @DisplayName("캐시에 일치하는 oidc 공개키가 없다면 다시 로드 후 정상 흐름으로 동작한다.")
    @Test
    fun signUpOrSignIn() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
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
        val fakeServiceToken = ServiceTokenVo(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        whenever(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())
        whenever(oidcHelper.parseKakaoIdToken(any(), any()))
            .thenReturn(fakeKakaoIdTokenPayload)

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
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName), never())!!.evictIfPresent(user.platform.name)
        assertThat(result.nickname).isEqualTo(user.nickname)
        assertThat(result.accessToken).isEqualTo(fakeServiceToken.accessToken)
        assertThat(result.refreshToken).isEqualTo(fakeServiceToken.refreshToken)
    }

    @DisplayName("캐시에 일치하는 oidc 공개키가 없다면 다시 로드 후 정상 흐름으로 동작한다.")
    @Test
    fun signUpOrSignIn_WithExpiredOidcPublicKey() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
        val exception = OidcPublicKeyMismatchException(ILLEGAL_KID)
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
        val fakeServiceToken = ServiceTokenVo(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        whenever(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())

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
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName), times(1))!!.evictIfPresent(user.platform.name)
        assertThat(result.nickname).isEqualTo(user.nickname)
        assertThat(result.accessToken).isEqualTo(fakeServiceToken.accessToken)
        assertThat(result.refreshToken).isEqualTo(fakeServiceToken.refreshToken)
    }

    @DisplayName("서버에 OIDC 공개키가 캐시되어있지 않을 경우, evict를 하지 않는다.")
    @Test
    fun signUpOrSignIn_whenOIDCPublicKeyNotExists_thenPassEvict() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
        val exception = OidcPublicKeyMismatchException(ILLEGAL_KID)
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
        val fakeServiceToken = ServiceTokenVo(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        whenever(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())

        whenever(oidcHelper.parseKakaoIdToken(any(), any()))
            .thenThrow(exception)  // Oidc PublicKey가 만료되어 불일치 발생
            .thenReturn(fakeKakaoIdTokenPayload)  // 캐싱 후 다시 정상값 반환

        doReturn(fakeServiceToken.accessToken)
            .whenever(jwtHelper).createAccessToken(user.id)
        doReturn(fakeServiceToken.refreshToken)
            .whenever(jwtHelper).createRefreshToken()
        whenever(oidcCacheManager.getCache(oidcPublicKeyCacheName))
            .thenReturn(null)

        // when
        val result = authService.signUpOrSignIn(
            loginPlatform = user.platform,
            idToken = idToken,
            deviceId = deviceId,
        )

        // then
        assertThat(result.nickname).isEqualTo(user.nickname)
        assertThat(result.accessToken).isEqualTo(fakeServiceToken.accessToken)
        assertThat(result.refreshToken).isEqualTo(fakeServiceToken.refreshToken)
    }

    @DisplayName("로그인 시 id-token이 잘못된 kid를 가지고 있을 경우 한번 재시도 후 예외를 반환한다.")
    @Test
    fun signUpOrSignIn_whenInvalidOidcPublicKey_thenThrowException() {
        // given
        val oidcPublicKeyCacheName = "oidc-public-key"
        val idToken = "idTokenWithPublicKeyIssue"
        val deviceId = "test-device"
        val exception = OidcPublicKeyMismatchException(ILLEGAL_KID)
        val user = userRepository.save(
            User(
                nickname = "testuser",
                platform = LoginPlatform.KAKAO,
                platformUserId = "test-kakao-user-id",
                userStatus = UserStatus.SINGLE,
            )
        )
        val fakeServiceToken = ServiceTokenVo(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
        )

        whenever(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())

        whenever(oidcHelper.parseKakaoIdToken(any(), any()))
            .thenThrow(exception)  // 잘못된 kid로 인해 public key 불일치 발생

        doReturn(fakeServiceToken.accessToken)
            .whenever(jwtHelper).createAccessToken(user.id)
        doReturn(fakeServiceToken.refreshToken)
            .whenever(jwtHelper).createRefreshToken()
        whenever(oidcCacheManager.getCache(oidcPublicKeyCacheName))
            .thenReturn(mock(Cache::class.java))

        // when
        val result = assertThrows<IllegalOidcTokenException> {
            authService.signUpOrSignIn(
                loginPlatform = user.platform,
                idToken = idToken,
                deviceId = deviceId,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_KID)
        verify(oidcCacheManager.getCache(oidcPublicKeyCacheName), times(1))!!.evictIfPresent(user.platform.name)
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

        doThrow(GlobalException(errorCode = GlobalExceptionCode.UNKNOWN))
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

    @DisplayName("회원 탈퇴 시 유저가 없다면 예외를 반환한다.")
    @Test
    fun deleteUser_whenUserNotExists_thenThrowException() {
        // given, when
        val result = assertThrows<UserNotFoundException> {
            authService.deleteUser(
                userId = 0L,  // Invalid user id
                bearerAccessToken = "${BEARER_TYPE}temp-access-token",
                deviceId = "test-device-id"
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(NOT_FOUND)
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
