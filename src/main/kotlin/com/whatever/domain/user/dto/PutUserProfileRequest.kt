package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

@Schema(description = "유저 프로필 수정 요청 DTO")
data class PutUserProfileRequest(
    @Schema(description = "수정할 닉네임. 한글과 영어로 이루어진 2-8자의 문자열.", nullable = true)
    @field:Pattern(
        regexp = "^$|[가-힣A-Za-z0-9]{2,8}$",
        message = "닉네임은 2~8자의 한글, 영문, 숫자로만 입력해주세요."
    )
    val nickname: String?,

    @Schema(description = "생일", nullable = true)
    val birthday: LocalDate?,
)