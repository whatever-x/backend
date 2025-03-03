package com.whatever.domain.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ServiceToken(
    @Schema(description = "서버 Access Token 입니다. API 요청에 사용됩니다.")
    val accessToken: String,
    @Schema(description = "서버 Refresh Token 입니다. Access Token 갱신에 사용됩니다.")
    val refreshToken: String,
)
