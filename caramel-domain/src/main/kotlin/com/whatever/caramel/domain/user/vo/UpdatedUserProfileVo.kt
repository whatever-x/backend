package com.whatever.caramel.domain.user.vo

import java.time.LocalDate

data class UpdatedUserProfileVo(
    val id: Long,
    val nickname: String,
    val birthday: LocalDate,
) {
    companion object {
        fun from(user: com.whatever.caramel.domain.user.model.User, id: Long): UpdatedUserProfileVo {
            return UpdatedUserProfileVo(
                id = id,
                nickname = user.nickname!!,
                birthday = user.birthDate!!,
            )
        }
    }
} 
