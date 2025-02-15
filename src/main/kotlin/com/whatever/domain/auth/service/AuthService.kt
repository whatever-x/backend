package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.dto.SocialAuthResponse
import com.whatever.domain.auth.service.provider.SocialUserProvider
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration


@Service
class AuthService(
    private val jwtHelper: JwtHelper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties,
    userProviders: List<SocialUserProvider>,
) {
    private val userProviderMap = userProviders.associateBy { it.platform }

    fun signUpOrSignIn(
        loginPlatform: LoginPlatform,
        accessToken: String,
    ): SocialAuthResponse {
        val userProvider = userProviderMap[loginPlatform]
            ?: throw GlobalException(
                GlobalExceptionCode.ARGS_VALIDATION_FAILED,
                "일치하는 로그인 플랫폼이 없습니다. platform: ${loginPlatform}"
            )

        val userId = userProvider.findOrCreateUser(accessToken).id
            ?: throw GlobalException(GlobalExceptionCode.ARGS_VALIDATION_FAILED)

        return createTokenAndSave(userId = userId)
    }

    private fun createTokenAndSave(userId: Long): SocialAuthResponse {
        val accessToken = jwtHelper.createAccessToken(userId)  // access token 발행
        val refreshToken = jwtHelper.createRefreshToken()  // refresh token 발행
        redisTemplate.opsForValue()
            .set(userId.toString(), refreshToken, Duration.ofSeconds(jwtProperties.refreshExpirationSec))
        return SocialAuthResponse(
            accessToken,
            refreshToken,
        )
    }
}