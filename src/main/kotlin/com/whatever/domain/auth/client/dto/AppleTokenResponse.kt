package com.whatever.domain.auth.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(SnakeCaseStrategy::class)
data class AppleTokenResponse(
    val accessToken: String,
    val expiresIn: Int,
    val idToken: String,
    val refreshToken: String? = null, // authorization_code 요청 시에만 반환될 수 있으므로 nullable
    val tokenType: String,
)