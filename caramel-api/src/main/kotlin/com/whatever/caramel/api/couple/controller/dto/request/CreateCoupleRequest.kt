package com.whatever.caramel.api.couple.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커플 생성(초대코드 등록) 요청 DTO")
data class CreateCoupleRequest(
    @Schema(description = "초대 코드")
    val invitationCode: String,
)
