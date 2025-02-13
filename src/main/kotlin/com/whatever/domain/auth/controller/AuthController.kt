package com.whatever.domain.auth.controller

import com.whatever.domain.auth.dto.SocialAuthResponse
import com.whatever.domain.auth.service.AuthService
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Auth",
    description = "인증 API"
)
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "회원가입",
        description = "소셜 토큰",
        responses = [
            ApiResponse(responseCode = "200", description = "성공"),
            ApiResponse(responseCode = "403", description = "유효하지 않은 토큰"),
        ]
    )
    @PostMapping("/sign-up")
    fun signUp(
        loginPlatform: LoginPlatform,
        accessToken: String,
    ): CaramelApiResponse<SocialAuthResponse> {
        val socialAuthResponse = authService.signUpOrSignIn(loginPlatform, accessToken)
        return socialAuthResponse.succeed()
    }
}