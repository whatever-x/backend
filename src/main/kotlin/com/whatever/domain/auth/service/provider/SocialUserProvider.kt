package com.whatever.domain.auth.service.provider

import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User

interface SocialUserProvider {
    val platform: LoginPlatform

    /**
     * 각 플랫폼의 AccessToken을 사용하여 가입된 User를 찾거나, 새로운 User를 저장하여 반환합니다.
     */
    fun findOrCreateUser(socialAccessToken: String): User
}
