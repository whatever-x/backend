package com.whatever.domain.auth.controller

import com.whatever.domain.auth.dto.ServiceToken
import com.whatever.domain.auth.dto.SignInRequest
import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.auth.service.AuthService
import com.whatever.global.annotation.DisableSwaggerAuthButton
import com.whatever.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
    @DisableSwaggerAuthButton
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
    fun signIn(
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
        @RequestBody request: SignInRequest,
    ): CaramelApiResponse<SignInResponse> {
        val socialAuthResponse = authService.signUpOrSignIn(
            loginPlatform = request.loginPlatform,
            idToken = request.idToken,
            deviceId = deviceId
        )
        return socialAuthResponse.succeed()
    }

    @Operation(
        summary = "로그아웃",
        description = "성공 응답이 나가면, 클라이언트에서 사용하던 access & refresh token은 재사용 할 수 없습니다."
    )
    @PostMapping("/sign-out")
    fun signOut(
        @RequestHeader(name = AUTH_JWT_HEADER, required = true) bearerAccessToken: String,
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
    ): CaramelApiResponse<Unit> {
        authService.signOut(
            bearerAccessToken = bearerAccessToken,
            deviceId = deviceId
        )
        return CaramelApiResponse.succeed()
    }

    @DisableSwaggerAuthButton
    @Operation(
        summary = "토큰 refresh",
        description = "새로운 accesstoken, refreshtoken 을 재발급합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "발급 완료"),
            ApiResponse(responseCode = "403", description = "리프레시 토큰 만료"),
        ]
    )
    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
        @RequestBody request: ServiceToken,
    ): CaramelApiResponse<ServiceToken> {
        val serviceToken = authService.refresh(request, deviceId)
        return serviceToken.succeed()
    }
}