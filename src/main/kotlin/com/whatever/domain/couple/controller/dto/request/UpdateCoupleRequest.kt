package com.whatever.domain.couple.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import java.time.LocalDate

@Schema(description = "커플 시작일 수정 요청 모델")
data class UpdateCoupleStartDateRequest(
    val startDate: LocalDate
)

@Schema(description = "커플 공유 메시지 수정 요청 모델")
data class UpdateCoupleSharedMessageRequest(
    @Schema(
        description = "공유 메시지. Allow Null, Not Blank",
        requiredMode = REQUIRED
    )
    val sharedMessage: String?,
)