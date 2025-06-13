package com.whatever.domain.couple.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커플 생성(초대코드 등록) 요청 모델")
data class CreateCoupleRequest(
    val invitationCode: String,
)