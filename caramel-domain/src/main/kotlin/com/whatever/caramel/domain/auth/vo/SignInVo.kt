package com.whatever.caramel.domain.auth.vo

import java.time.LocalDate

data class SignInVo(
    val accessToken: String,
    val refreshToken: String,
    val userStatus: String,
    val nickname: String?,
    val birthDay: LocalDate?,
    val coupleId: Long?,
) {
    companion object {
        fun from(serviceToken: ServiceTokenVo, user: com.whatever.caramel.domain.user.model.User): SignInVo {
            return SignInVo(
                accessToken = serviceToken.accessToken,
                refreshToken = serviceToken.refreshToken,
                userStatus = user.userStatus.name,
                nickname = user.nickname,
                birthDay = user.birthDate,
                coupleId = user.couple?.id
            )
        }
    }
}
