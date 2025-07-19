package com.whatever.domain.couple.vo

import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserGender
import com.whatever.domain.user.model.UserStatus
import java.time.LocalDate

data class CoupleUserInfoVo(
    val id: Long,
    val userStatus: UserStatus,
    val nickname: String,
    val birthDate: LocalDate,
    val gender: UserGender,
) {
    companion object {
        fun from(user: User): CoupleUserInfoVo {
            return CoupleUserInfoVo(
                id = user.id,
                userStatus = user.userStatus,
                nickname = user.nickname!!,
                birthDate = user.birthDate!!,
                gender = user.gender!!,
            )
        }
    }
} 