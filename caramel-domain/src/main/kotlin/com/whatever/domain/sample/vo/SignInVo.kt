package com.whatever.domain.sample.vo

import com.whatever.domain.auth.vo.ServiceTokenVo
import com.whatever.domain.user.model.User
import com.whatever.domain.user.vo.UserInfoVo

data class SignInVo(
    val userInfo: UserInfoVo,
    val serviceToken: ServiceTokenVo,
) {
    companion object {
        fun from(
            user: User,
            accessToken: String,
            refreshToken: String,
        ): SignInVo {
            return SignInVo(
                userInfo = UserInfoVo.from(user),
                serviceToken = ServiceTokenVo.from(accessToken, refreshToken)
            )
        }
    }
}