package com.whatever.domain.auth.service.provider

import com.whatever.domain.auth.client.KakaoOIDCClient
import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.domain.auth.service.OIDCHelper
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class KakaoUserProvider(
    private val kakaoOIDCClient: KakaoOIDCClient,
    private val userRepository: UserRepository,
    private val oidcHelper: OIDCHelper,
) : SocialUserProvider {

    override val platform: LoginPlatform
        get() = LoginPlatform.KAKAO

    override fun findOrCreateUser(socialIdToken: String): User {
        val kakaoPublicKey = kakaoOIDCClient.getOIDCPublicKey()
        val idTokenPayload = oidcHelper.parseKakaoIdToken(
            idToken = socialIdToken,
            oidcPublicKeys = kakaoPublicKey.keys,
        )

        userRepository.findByPlatformUserId(idTokenPayload.platformUserId)?.let {
            return it
        }

        return try {
            userRepository.save(idTokenPayload.toUser())
        } catch (e: DataIntegrityViolationException) {
            userRepository.findByPlatformUserId(idTokenPayload.platformUserId) ?: throw e
        }
    }
}

private fun KakaoIdTokenPayload.toUser() = User(
    platform = LoginPlatform.KAKAO,
    platformUserId = platformUserId,
    nickname = nickname,
    email = email,
)
