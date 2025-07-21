package com.whatever.domain.auth.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.jwt.JwtHelper
import com.whatever.caramel.common.global.jwt.JwtHelper.Companion.BEARER_TYPE
import com.whatever.caramel.common.global.jwt.JwtProperties
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.domain.auth.exception.AuthException
import com.whatever.domain.auth.exception.AuthExceptionCode
import com.whatever.domain.auth.exception.AuthExceptionCode.USER_PROVIDER_NOT_FOUND
import com.whatever.domain.auth.exception.AuthFailedException
import com.whatever.domain.auth.exception.IllegalOidcTokenException
import com.whatever.domain.auth.exception.OidcPublicKeyMismatchException
import com.whatever.domain.auth.repository.AuthRedisRepository
import com.whatever.domain.auth.service.provider.SocialUserProvider
import com.whatever.domain.auth.vo.ServiceTokenVo
import com.whatever.domain.auth.vo.SignInVo
import com.whatever.domain.couple.service.CoupleService
import com.whatever.domain.findByIdAndNotDeleted
import com.whatever.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.domain.user.exception.UserNotFoundException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

@Service
class AuthService(
    private val jwtHelper: JwtHelper,
    private val jwtProperties: JwtProperties,
    private val authRedisRepository: AuthRedisRepository,
    userProviders: List<SocialUserProvider>,
    @Qualifier("oidcCacheManager") private val oidcCacheManager: CacheManager,
    private val userRepository: UserRepository,
    private val coupleService: CoupleService,
) {
    private val userProviderMap = userProviders.associateBy { it.platform }

    fun signUpOrSignIn(
        loginPlatform: LoginPlatform,
        idToken: String,
        deviceId: String,
    ): SignInVo {
        val userProvider = userProviderMap[loginPlatform]
            ?: throw AuthFailedException(
                errorCode = USER_PROVIDER_NOT_FOUND,
                errorUi = ErrorUi.Dialog("로그인을 하지 못 했어요.\n다시 한 번 시도해 주세요."),
            )

        val user = runCatching { userProvider.findOrCreateUser(idToken) }
            .recoverCatching { exception ->
                if (exception !is OidcPublicKeyMismatchException) {
                    throw exception
                }
                oidcCacheManager.evictOidcPublicKeyCache(loginPlatform)
                userProvider.findOrCreateUser(idToken)
            }.getOrElse { exception ->
                if (exception !is OidcPublicKeyMismatchException) {
                    logger.warn(exception) { "User sign-in failed." }
                    throw AuthFailedException(
                        errorCode = AuthExceptionCode.UNKNOWN,
                        errorUi = ErrorUi.Dialog("로그인을 하지 못 했어요.\n다시 한 번 시도해 주세요."),
                    )
                }
                throw IllegalOidcTokenException(
                    errorCode = AuthExceptionCode.ILLEGAL_KID,
                    errorUi = ErrorUi.Dialog("로그인을 하지 못 했어요.\n다시 한 번 시도해 주세요."),
                )
            }

        val userId = user.id

        val serviceToken = createTokenAndSave(userId = userId, deviceId = deviceId)
        return SignInVo.from(
            serviceToken = serviceToken,
            user = user
        )
    }

    fun signOut(
        bearerAccessToken: String,
        deviceId: String,
        userId: Long,
    ) {
        logger.debug { "SignOut Start - UserId: $userId, DeviceId: $deviceId" }

        val accessToken = bearerAccessToken.substring(BEARER_TYPE.length)

        try {
            val jti = jwtHelper.extractJti(accessToken)
            val expDateTime = jwtHelper.extractExpDate(accessToken).toInstant().atZone(DateTimeUtil.UTC_ZONE_ID)
            val expirationDuration = DateTimeUtil.getDuration(endDateTime = expDateTime)

            authRedisRepository.saveJtiToBlacklist(jti = jti, expirationDuration = expirationDuration)
        } catch (e: ExpiredJwtException) {
            logger.debug { "Access Token is already expired - UserId: $userId" }
        }

        authRedisRepository.deleteRefreshToken(userId = userId, deviceId = deviceId)

        logger.debug { "SignOut End - UserId: $userId, DeviceId: $deviceId" }
    }

    fun refresh(accessToken: String, refreshToken: String, deviceId: String): ServiceTokenVo {
        val userId = jwtHelper.extractUserIdIgnoringSignature(accessToken)
        val isValid = jwtHelper.isValidJwt(refreshToken)

        if (isValid.not()) throw AuthException(errorCode = AuthExceptionCode.UNAUTHORIZED)

        val localRefreshToken = authRedisRepository.getRefreshToken(userId = userId, deviceId = deviceId)

        if (refreshToken != localRefreshToken) {
            throw AuthException(errorCode = AuthExceptionCode.UNAUTHORIZED)
        }
        // TODO(준용) access token black list 등록

        return createTokenAndSave(userId = userId, deviceId = deviceId)
    }

    @Transactional
    fun deleteUser(
        bearerAccessToken: String,
        deviceId: String,
        userId: Long,
    ) {
        val user = userRepository.findByIdAndNotDeleted(userId)
            ?: throw UserNotFoundException(errorCode = NOT_FOUND)

        user.couple?.run {  // 커플이 있다면 탈퇴 진행
            coupleService.leaveCouple(
                coupleId = id,
                userId = user.id,
            )
        }
        userProviderMap[user.platform]?.unlinkUser(userId)
        user.deleteEntity()
        signOut(
            // 인증 토큰 제거
            bearerAccessToken = bearerAccessToken,
            deviceId = deviceId,
            userId = userId,
        )
        authRedisRepository.deleteAllRefreshToken(userId)
    }

    private fun createTokenAndSave(userId: Long, deviceId: String): ServiceTokenVo {
        val accessToken = jwtHelper.createAccessToken(userId)  // access token 발행
        val refreshToken = jwtHelper.createRefreshToken()  // refresh token 발행
        authRedisRepository.saveRefreshToken(
            userId = userId,
            deviceId = deviceId,
            refreshToken = refreshToken,
            ttlSeconds = jwtProperties.refreshExpirationSec
        )
        return ServiceTokenVo.from(
            accessToken,
            refreshToken,
        )
    }
}

private fun CacheManager.evictOidcPublicKeyCache(loginPlatform: LoginPlatform) {
    getCache("oidc-public-key")?.also {
        it.evictIfPresent(loginPlatform.name)
        logger.info { "${loginPlatform.name} OIDC Public Key cache eviction completed." }
    } ?: logger.debug { "Cache 'oidc-public-key' not available. Skipping eviction for ${loginPlatform.name}." }
}
