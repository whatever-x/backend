package com.whatever.domain.auth.service.provider

import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import org.springframework.stereotype.Component

@Component
class AppleUserProvider : SocialUserProvider {

    override val platform: LoginPlatform
        get() = LoginPlatform.APPLE

    override fun findOrCreateUser(socialAccessToken: String): User {
        // TODO(준용): Apple 유저
        throw IllegalStateException("미구현된 기능합니다.")
    }
}
