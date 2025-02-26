package com.whatever.domain.auth.dto

import com.whatever.domain.user.model.UserStatus
import java.time.LocalDate

data class SignInResponse(
    val serviceToken: ServiceToken,
    val userStatus: UserStatus,
    val nickname: String?,
    val birthDay: LocalDate?,
)