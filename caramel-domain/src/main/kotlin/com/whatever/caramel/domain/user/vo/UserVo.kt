package com.whatever.caramel.domain.user.vo

import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import java.time.LocalDate

data class UserVo(
    val id: Long,
    val email: String?,
    val birthDate: LocalDate?,
    val signInPlatform: LoginPlatform,
    val signInPlatformId: String,
    val nickname: String?,
    val gender: UserGender?,
    val userStatus: UserStatus,
    val coupleId: Long,
) {
    companion object {
        fun from(user: com.whatever.caramel.domain.user.model.User): UserVo {
            return UserVo(
                id = user.id,
                email = user.email,
                birthDate = user.birthDate,
                signInPlatform = user.platform,
                signInPlatformId = user.platformUserId,
                nickname = user.nickname,
                gender = user.gender,
                userStatus = user.userStatus,
                coupleId = user.couple?.id ?: 0L,
            )
        }
    }
} 
