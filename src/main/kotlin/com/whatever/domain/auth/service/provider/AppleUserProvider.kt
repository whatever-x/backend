package com.whatever.domain.auth.service.provider

import com.whatever.domain.auth.client.AppleOIDCClient
import com.whatever.domain.auth.client.dto.AppleIdTokenPayload
import com.whatever.domain.auth.dto.AppleAuthFormData
import com.whatever.domain.auth.dto.AppleUserName
import com.whatever.domain.auth.service.OIDCHelper
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class AppleUserProvider(
    private val appleOIDCClient: AppleOIDCClient,
    private val oidcHelper: OIDCHelper,
    private val userRepository: UserRepository,
) : SocialUserProvider {

    override val platform: LoginPlatform
        get() = LoginPlatform.APPLE

    override fun findOrCreateUser(socialAccessToken: String): User {
        // TODO(준용): Apple 유저
        throw IllegalStateException("미구현된 기능합니다.")
    }

    fun findOrCreateUserByAppleAuthFormData(appleAuthFormData: AppleAuthFormData): User {
        return findOrCreateUserByIdToken(
            socialIdToken = appleAuthFormData.idToken,
            userName = appleAuthFormData.appleUser?.name
        )
    }

    fun findOrCreateUserByIdToken(socialIdToken: String, userName: AppleUserName? = null): User {
        val keys = appleOIDCClient.getOIDCPublicKey().keys

        val payload = oidcHelper.parseAppleIdToken(socialIdToken, keys)

        userRepository.findByPlatformUserId(payload.platformUserId)?.let {
            return it
        }

        return try {
            userRepository.save(payload.toUser(userName))
        } catch (e: DataIntegrityViolationException) {
            userRepository.findByPlatformUserId(payload.platformUserId) ?: throw e
        }
    }
}

private fun AppleIdTokenPayload.toUser(userName: AppleUserName? = null) = User(
    platform = LoginPlatform.APPLE,
    platformUserId = sub,
    email = email,
    nickname = userName?.toString()
)