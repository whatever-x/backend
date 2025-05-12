package com.whatever.domain.auth.service.provider

import com.whatever.config.properties.OauthProperties
import com.whatever.domain.auth.client.KakaoKapiClient
import com.whatever.domain.auth.client.KakaoOIDCClient
import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.domain.auth.client.dto.KakaoUnlinkUserRequest
import com.whatever.domain.auth.service.OIDCHelper
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserExceptionCode.NOT_FOUND
import com.whatever.domain.user.exception.UserNotFoundException
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.findByIdAndNotDeleted
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }

@Component
class KakaoUserProvider(
    private val kakaoOIDCClient: KakaoOIDCClient,
    private val userRepository: UserRepository,
    private val oidcHelper: OIDCHelper,
    private val kakaoKapiClient: KakaoKapiClient,
    private val oauthProperties: OauthProperties,
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

    override fun unlinkUser(userId: Long) {
        val user = userRepository.findByIdAndNotDeleted(userId)
            ?: throw UserNotFoundException(errorCode = NOT_FOUND)

        runCatching {
            kakaoKapiClient.unlinkUserByAdminKey(
                appAdminKeyWithPrefix = oauthProperties.kakao.adminKeyWithPrefix,
                unlinkUser = KakaoUnlinkUserRequest(
                    targetId = user.platformUserId.toLong()
                )
            )
        }
            .onSuccess { res -> logger.debug { "Kakao user unlinked. userId: ${userId}, platformId: ${res.id}" } }
            .onFailure { e -> logger.debug(e) { "Kakao user unlink failed. userId: ${userId}" } }
    }
}

private fun KakaoIdTokenPayload.toUser() = User(
    platform = LoginPlatform.KAKAO,
    platformUserId = platformUserId,
    nickname = nickname,
    email = email,
)
