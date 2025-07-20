package com.whatever.caramel.api.couple.controller.dto.request

import com.whatever.domain.couple.model.Couple.Companion.MAX_SHARED_MESSAGE_LENGTH
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDate

@Schema(description = "커플 시작일 수정 요청 DTO")
data class UpdateCoupleStartDateRequest(
    val startDate: LocalDate,
)

@Schema(description = "커플 공유 메시지 수정 요청 DTO")
data class UpdateCoupleSharedMessageRequest(
    @Schema(
        description = "수정할 공유 메시지. Null이나 Blank는 공유 메시지가 없는 상태로 취급.",
        nullable = true
    )
    @field:CodePointLength(
        max = MAX_SHARED_MESSAGE_LENGTH,
        message = "Maximum description length is ${MAX_SHARED_MESSAGE_LENGTH}"
    )
    val sharedMessage: String?,
)
