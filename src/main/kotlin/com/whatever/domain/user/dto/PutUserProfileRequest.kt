package com.whatever.domain.user.dto

import com.whatever.domain.user.model.User.Companion.MAX_NICKNAME_LENGTH
import com.whatever.domain.user.model.User.Companion.MIN_NICKNAME_LENGTH
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "유저 프로필 수정 요청 DTO")
data class PutUserProfileRequest(
    @Schema(
        description = "수정할 닉네임. 한글과 영어, 숫자로 이루어진 ${MIN_NICKNAME_LENGTH}-${MAX_NICKNAME_LENGTH}자의 문자열.",
        nullable = true
    )
    @field:Size(
        min = MIN_NICKNAME_LENGTH,
        max = MAX_NICKNAME_LENGTH,
        message = "닉네임은 ${MIN_NICKNAME_LENGTH}~${MAX_NICKNAME_LENGTH}자 이내로 입력해주세요."
    )
    @field:Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자로만 입력해주세요.")

    val nickname: String?,

    @Schema(description = "생일", nullable = true)
    val birthday: LocalDate?,
)
