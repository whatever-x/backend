package com.whatever.caramel.domain.auth.service.provider

import com.whatever.caramel.domain.auth.service.OIDCHelper
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User.Companion.MAX_NICKNAME_LENGTH
import com.whatever.caramel.domain.user.model.User.Companion.MIN_NICKNAME_LENGTH
import com.whatever.caramel.domain.user.repository.UserRepository
import com.whatever.caramel.infrastructure.client.AppleOIDCClient
import com.whatever.caramel.infrastructure.client.dto.AppleAuthFormData
import com.whatever.caramel.infrastructure.client.dto.AppleIdTokenPayload
import com.whatever.caramel.infrastructure.client.dto.AppleUserName
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
class AppleUserProvider(
    private val appleOIDCClient: AppleOIDCClient,
    private val oidcHelper: OIDCHelper,
    private val userRepository: UserRepository,
) : SocialUserProvider {

    override val platform: LoginPlatform
        get() = LoginPlatform.APPLE

    override fun findOrCreateUser(socialIdToken: String): com.whatever.caramel.domain.user.model.User {
        val applePublicKey = appleOIDCClient.getOIDCPublicKey()
        val idTokenPayload = oidcHelper.parseAppleIdToken(
            idToken = socialIdToken,
            oidcPublicKeys = applePublicKey.keys,
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
        logger.debug { "Bypass Apple user unlink. userId: ${userId}" }
    }

    fun findOrCreateUserByAppleAuthFormData(appleAuthFormData: AppleAuthFormData): com.whatever.caramel.domain.user.model.User {
        return findOrCreateUserByIdToken(
            socialIdToken = appleAuthFormData.idToken,
            userName = appleAuthFormData.appleUser?.name
        )
    }

    fun findOrCreateUserByIdToken(
        socialIdToken: String,
        userName: AppleUserName? = null,
    ): com.whatever.caramel.domain.user.model.User {
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

private fun AppleIdTokenPayload.toUser(userName: AppleUserName? = null) = com.whatever.caramel.domain.user.model.User(
    platform = LoginPlatform.APPLE,
    platformUserId = sub,
    email = email,
    nickname = userName?.toString()?.takeIf { it.trim().length in MIN_NICKNAME_LENGTH..MAX_NICKNAME_LENGTH }
)
