package com.whatever.domain.auth.service

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

interface AuthService {

    fun signUp(
        loginPlatform: LoginPlatform,
        accessToken: String,
    ): SocialAuthResponse
}

@Service
class DefaultAuthService(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val jwtHelper: JwtHelper,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
) : AuthService {

    override fun signUp(
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

        val jwtAccessToken = jwtHelper.createAccessToken(kakaoUserInfoResponse.id)
        val jwtRefreshToken = jwtHelper.createRefreshToken()

        return SocialAuthResponse(jwtAccessToken, jwtRefreshToken).also {
            redisTemplate.opsForValue().set(user.id.toString(), it.refreshToken, Duration.ofDays(7L))
        }
    }

    private fun getAppleAccessToken(accessToken: String): SocialAuthResponse {
        // TODO: 애플 로그인 구현 필요
        throw IllegalStateException("애플 로그인 미구현")
    }
}

private fun KakaoUserInfoResponse.toUser() = User(
    platform = LoginPlatform.KAKAO,
    nickname = kakaoAccount?.profile?.nickname,
    email = kakaoAccount?.email,
)