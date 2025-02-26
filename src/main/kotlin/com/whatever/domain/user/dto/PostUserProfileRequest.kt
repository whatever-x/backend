package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PostUserProfileRequest(
    @Schema(description = "닉네임")
    val nickname: String,
    @Schema(description = "YYYY-MM-DD 형식의 생일")
    val birthday: LocalDate,
    @Schema(description = "서비스/약관 동의 여부")
    val agreementServiceTerms: Boolean,
    @Schema(description = "개인정보 수집/이용 동의")
    val agreementPrivatePolicy: Boolean,
)
