package com.whatever.domain.auth.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import feign.form.FormProperty

data class KakaoIdTokenInfoRequest(
    @FormProperty("id_token")
    private var idToken: String
)

@JsonNaming(SnakeCaseStrategy::class)
data class KakaoIdTokenPayload(
    val iss: String,
    val aud: String,
    val sub: String,
    val iat: Long,
    val exp: Long,
    val authTime: Long,
    val nonce: String? = null,
    val nickname: String? = null,
    val picture: String? = null,
    val email: String? = null
)
