package com.whatever.domain.user.vo

import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import java.time.LocalDate

data class UserInfoVo(
    val id: Long,
    val email: String?,
    val birthDate: LocalDate?,
    val signInPlatform: LoginPlatform,
    val nickname: String?,
    val gender: UserGender?,
    val userStatus: UserStatus,
) {
    companion object {
        fun from(user: User): UserInfoVo {
            return UserInfoVo(
                id = user.id,
                email = user.email,
                birthDate = user.birthDate,
                signInPlatform = user.platform,
                nickname = user.nickname,
                gender = user.gender,
                userStatus = user.userStatus,
            )
        }
    }
} 