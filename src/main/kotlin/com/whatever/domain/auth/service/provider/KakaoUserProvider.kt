package com.whatever.domain.auth.service.provider

import com.whatever.domain.auth.client.KakaoOAuthClient
import com.whatever.domain.auth.client.dto.KakaoUserInfoResponse
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class KakaoUserProvider(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val userRepository: UserRepository,
) : SocialUserProvider {

    override val platform: LoginPlatform
        get() = LoginPlatform.KAKAO

    override fun findOrCreateUser(socialAccessToken: String): User {
        val userInfo = kakaoOAuthClient.getUserInfo(socialAccessToken)
        userRepository.findByPlatformUserId(userInfo.platformUserId)?.let {
            return it
        }

        return try {
            userRepository.save(userInfo.toUser())
        } catch (e: DataIntegrityViolationException) {
            userRepository.findByPlatformUserId(userInfo.platformUserId) ?: throw e
        }
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
