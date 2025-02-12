package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UserSignUpResponse(
    @Schema(description = "유저 ID")
    val userId: Long,
    @Schema(description = "유저 상태")
    val userStatus: UserStatus,
)