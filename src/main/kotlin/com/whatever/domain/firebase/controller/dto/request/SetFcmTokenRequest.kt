package com.whatever.domain.firebase.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class SetFcmTokenRequest(
    @Schema(description = "fcm token")
    val token: String,
)