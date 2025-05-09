package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.exception.AuthException
import com.whatever.domain.auth.exception.AuthExceptionCode
import com.whatever.domain.auth.exception.IllegalOidcTokenException
import com.whatever.domain.auth.exception.OidcPublicKeyMismatchException
import com.whatever.domain.auth.service.provider.SocialUserProvider
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.util.RedisUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

val logger = KotlinLogging.logger {  }

@Service
class AuthService(
    private val jwtHelper: JwtHelper,
    private val jwtProperties: JwtProperties,
    userProviders: List<SocialUserProvider>,
    private val redisUtil: RedisUtil,
    @Qualifier("oidcCacheManager") private val oidcCacheManager: CacheManager,
) {
    private val userProviderMap = userProviders.associateBy { it.platform }

    fun signUpOrSignIn(
        loginPlatform: LoginPlatform,
        idToken: String,
    ): SignInResponse {
        val userProvider = userProviderMap[loginPlatform]
            ?: throw GlobalException(
                errorCode = GlobalExceptionCode.ARGS_VALIDATION_FAILED,
                detailMessage = "일치하는 로그인 플랫폼이 없습니다. platform: $loginPlatform"
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
                    throw exception
                }
                throw IllegalOidcTokenException(
                    errorCode = AuthExceptionCode.ILLEGAL_KID,
                    detailMessage = "일치하는 Kid가 없는 idToken입니다."
                )
            }

        val userId = user.id
        val coupleId = user.couple?.id

        val serviceToken = createTokenAndSave(userId = userId)
        return SignInResponse(
            serviceToken = serviceToken,
            userStatus = user.userStatus,
            nickname = user.nickname,
            birthDay = user.birthDate,
            coupleId = coupleId,
        )
    }

    fun refresh(serviceToken: ServiceToken): ServiceToken {
        val userId = jwtHelper.extractUserIdIgnoringSignature(serviceToken.accessToken)
        val isValid = jwtHelper.isValidJwt(serviceToken.refreshToken)

        if (isValid.not()) throw AuthException(errorCode = AuthExceptionCode.UNAUTHORIZED)

        val refreshToken = redisUtil.getRefreshToken(userId = userId, deviceId = "tempDeviceId")

        if (serviceToken.refreshToken != refreshToken) {
            throw AuthException(errorCode = AuthExceptionCode.UNAUTHORIZED)
        }

        return createTokenAndSave(userId = userId)
    }

    private fun createTokenAndSave(userId: Long): ServiceToken {
        val accessToken = jwtHelper.createAccessToken(userId)  // access token 발행
        val refreshToken = jwtHelper.createRefreshToken()  // refresh token 발행
        redisUtil.saveRefreshToken(
            userId = userId,
            deviceId = "tempDeviceId",  // TODO(준용): Client에서 Device Id를 받아와 저장 필요
            refreshToken = refreshToken,
            ttlSeconds = jwtProperties.refreshExpirationSec
        )
        return ServiceToken(
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