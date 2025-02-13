package com.whatever.domain.auth.service

import com.whatever.config.properties.JwtProperties
import com.whatever.domain.auth.client.KakaoOAuthClient
import com.whatever.domain.auth.client.dto.KakaoUserInfoResponse
import com.whatever.domain.auth.dto.SocialAuthResponse
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate


@Service
class AuthService(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val jwtHelper: JwtHelper,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties
) {
    fun signUp(
        loginPlatform: LoginPlatform,
        accessToken: String,
    ): SocialAuthResponse {
        return when (loginPlatform) {
            LoginPlatform.KAKAO -> {
                getKakaoResponse(accessToken = accessToken)
            }

            LoginPlatform.APPLE -> {
                getAppleAccessToken(accessToken = accessToken)
            }

            else -> {
                throw GlobalException(GlobalExceptionCode.ARGS_VALIDATION_FAILED)
            }
        }
    }

    private fun getKakaoResponse(accessToken: String): SocialAuthResponse {
        val kakaoUserInfoResponse = kakaoOAuthClient.getUserInfo(accessToken)
        val user = userRepository.save(kakaoUserInfoResponse.toUser())
        val userId = user.id ?: throw GlobalException(GlobalExceptionCode.ARGS_VALIDATION_FAILED)

        return createTokenAndSave(userId = userId)
    }

    private fun getAppleAccessToken(accessToken: String): SocialAuthResponse {
        // TODO: 애플 로그인 구현 필요
        throw IllegalStateException("애플 로그인 미구현")
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

private fun KakaoUserInfoResponse.toUser() = User(
    platform = LoginPlatform.KAKAO,
    platformUserId = platformUserId,
    nickname = kakaoAccount.profile.nickname,
    email = kakaoAccount.email,
    birthDate = if (kakaoAccount.birthYear != null && kakaoAccount.birthDay != null) {
        LocalDate.of(
            kakaoAccount.birthYear.toInt(),
            kakaoAccount.birthDay.take(2).toInt(),
            kakaoAccount.birthDay.takeLast(2).toInt()
        )
    } else {
        null
    }
)
