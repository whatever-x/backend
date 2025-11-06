package com.whatever.caramel.domain.auth.vo

import com.whatever.caramel.domain.user.model.UserStatus

data class TokenRefreshVo(
    val serviceTokenVo: ServiceTokenVo,
    val userId: Long,
    val userStatus: UserStatus,
)
