package com.whatever.caramel.domain.user.vo

import com.whatever.caramel.domain.user.model.User
import java.time.LocalDate

data class UpdatedUserProfileVo(
    val id: Long,
    val nickname: String,
    val birthday: LocalDate,
) {
    companion object {
        fun from(user: User, id: Long): UpdatedUserProfileVo {
            return UpdatedUserProfileVo(
                id = id,
                nickname = user.nickname!!,
                birthday = user.birthDate!!,
            )
        }
    }
} 
