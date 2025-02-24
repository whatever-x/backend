package com.whatever.domain.auth.dto

import com.whatever.domain.user.dto.UserStatus
import java.time.LocalDate

data class SignInResponse(
    val serviceToken: ServiceToken,
    val userStatus: UserStatus,
    val nickname: String?,
    val birthDay: LocalDate?,
)