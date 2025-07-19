package com.whatever.domain.user.vo

import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus

data class CreatedUserProfileVo(
    val id: Long,
    val nickname: String,
    val userStatus: UserStatus,
) {
    companion object {
        fun from(user: User, id: Long): CreatedUserProfileVo {
            return CreatedUserProfileVo(
                id = id,
                nickname = user.nickname!!,
                userStatus = user.userStatus,
            )
        }
    }
} 