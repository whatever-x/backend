package com.whatever.com.whatever.caramel.api.auth.dto

import com.whatever.domain.user.model.LoginPlatform
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 요청 DTO")
data class SignInRequest(
    @Schema(description = "로그인 한 소셜 플랫폼")
    val loginPlatform: LoginPlatform,
    @Schema(description = "소셜 플랫폼에서 제공한 OIDC ID Token")
    val idToken: String,
)
