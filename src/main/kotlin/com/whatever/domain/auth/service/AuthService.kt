package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.service.provider.SocialUserProvider
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.util.RedisUtil
import org.springframework.stereotype.Service


@Service
class AuthService(
    private val jwtHelper: JwtHelper,
    private val jwtProperties: JwtProperties,
    userProviders: List<SocialUserProvider>,
    private val redisUtil: RedisUtil,
) {
    private val userProviderMap = userProviders.associateBy { it.platform }

    fun signUpOrSignIn(
        loginPlatform: LoginPlatform,
        accessToken: String,
    ): SignInResponse {
        val userProvider = userProviderMap[loginPlatform]
            ?: throw GlobalException(
                errorCode = GlobalExceptionCode.ARGS_VALIDATION_FAILED,
                detailMessage = "일치하는 로그인 플랫폼이 없습니다. platform: $loginPlatform"
            )

        val user = userProvider.findOrCreateUser(accessToken)
        val userId = user.id ?: throw GlobalException(GlobalExceptionCode.ARGS_VALIDATION_FAILED)

        val serviceToken = createTokenAndSave(userId = userId)
        return SignInResponse(
            serviceToken = serviceToken,
            userStatus = user.userStatus,
            nickname = user.nickname,
            birthDay = user.birthDate,
        )
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