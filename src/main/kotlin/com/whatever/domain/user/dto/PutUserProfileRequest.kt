package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class PutUserProfileRequest(
    @Schema(description = "닉네임")
    @field:NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @field:Size(min = 3, max = 10, message = "닉네임은 3~10자 이내로 입력해주세요.")
    val nickname: String?,

    @Schema(description = "생일")
    val birthday: LocalDate?,
)