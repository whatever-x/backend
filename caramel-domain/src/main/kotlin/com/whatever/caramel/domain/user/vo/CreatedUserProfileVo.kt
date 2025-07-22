package com.whatever.caramel.domain.user.vo

import com.whatever.caramel.domain.user.model.UserStatus

data class CreatedUserProfileVo(
    val id: Long,
    val nickname: String,
    val userStatus: UserStatus,
) {
    companion object {
        fun from(user: com.whatever.caramel.domain.user.model.User, id: Long): CreatedUserProfileVo {
            return CreatedUserProfileVo(
                id = id,
                nickname = user.nickname!!,
                userStatus = user.userStatus,
            )
        }
    }
} 
