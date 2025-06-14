package com.whatever.domain.couple.controller.dto.request

import com.whatever.domain.couple.model.Couple.Companion.MAX_SHARED_MESSAGE_LENGTH
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.CodePointLength
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
    @field:CodePointLength(max = MAX_SHARED_MESSAGE_LENGTH, message = "Maximum description length is ${MAX_SHARED_MESSAGE_LENGTH}")
    val sharedMessage: String?,
)