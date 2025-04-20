package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class PutUserProfileRequest(
    @Schema(description = "닉네임")
    @field:Pattern(
        regexp = "^$|[가-힣A-Za-z0-9]{3,10}$",
        message = "닉네임은 3~10자의 한글, 영문, 숫자로만 입력해주세요."
    )
    val nickname: String?,

    @Schema(description = "생일")
    val birthday: LocalDate?,
)