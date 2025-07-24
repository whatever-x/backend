package com.whatever.caramel.domain.auth.service.provider

import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User

interface SocialUserProvider {
    val platform: LoginPlatform

    /**
     * 각 플랫폼의 AccessToken을 사용하여 가입된 User를 찾거나, 새로운 User를 저장하여 반환합니다.
     */
    fun findOrCreateUser(socialIdToken: String): User

    /**
     * 각 유저 소셜 플랫폼의 unlink를 진행합니다.
     */
    fun unlinkUser(userId: Long)
}
