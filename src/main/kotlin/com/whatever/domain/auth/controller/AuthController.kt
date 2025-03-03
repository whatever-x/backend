package com.whatever.domain.auth.controller

import com.whatever.domain.auth.dto.SignInRequest
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.service.AuthService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Auth",
    description = "인증 API"
)
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "로그인 or 회원가입",
        description = "이미 회원인 경우 로그인을, 그렇지 않다면 회원가입을 진행합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "201", description = "회원가입 성공"),
            ApiResponse(responseCode = "403", description = "유효하지 않은 토큰"),
        ]
    )
    @PostMapping("/sign-in")
    fun signUp(
        @RequestBody request: SignInRequest,
    ): CaramelApiResponse<SignInResponse> {
        val socialAuthResponse = authService.signUpOrSignIn(
            loginPlatform = request.loginPlatform,
            accessToken = request.idToken
        )
        return socialAuthResponse.succeed()
    }
}