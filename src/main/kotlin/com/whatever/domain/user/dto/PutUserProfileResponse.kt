package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PutUserProfileResponse(
    @Schema(description = "유저 아이디")
    val id: Long,
    @Schema(description = "닉네임")
    val nickname: String,
    @Schema(description = "생일")
    val birthday: LocalDate,
)