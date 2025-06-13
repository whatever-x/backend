package com.whatever.domain.couple.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "커플 초대코드 응답 모델")
data class CoupleInvitationCodeResponse(
    val invitationCode: String,
    val expirationDateTime: LocalDateTime?
)
