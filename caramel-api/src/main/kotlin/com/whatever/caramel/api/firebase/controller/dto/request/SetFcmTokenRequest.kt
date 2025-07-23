package com.whatever.caramel.api.firebase.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Fcm 토큰 수정 요청 DTO")
data class SetFcmTokenRequest(
    @Schema(description = "fcm token")
    val token: String,
)
