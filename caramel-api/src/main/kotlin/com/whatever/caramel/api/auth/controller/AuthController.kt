package com.whatever.caramel.api.auth.controller

import com.whatever.CaramelApiResponse
import com.whatever.SecurityUtil.getCurrentUserId
import com.whatever.caramel.api.auth.dto.ServiceTokenResponse
import com.whatever.caramel.api.auth.dto.SignInRequest
import com.whatever.caramel.api.auth.dto.SignInResponse
import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.AUTH_JWT_HEADER
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.domain.auth.service.AuthService
import com.whatever.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.math.sign

@Tag(
    name = "인증 API",
    description = "로그인, 토큰 갱신 등 인증과 관련된 API"
)
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @DisableSwaggerAuthButton
    @Operation(
        summary = "로그인 or 회원가입",
        description = """
            ### 이미 회원인 경우 로그인을, 그렇지 않다면 회원가입을 진행합니다.
            
            - 회원가입을 진행한 유저는 NEW 상태로 생성되며, 프로필 생성을 진행해야 합니다.
        """,
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
        val signInVo = authService.signUpOrSignIn(
            loginPlatform = request.loginPlatform,
            idToken = request.idToken,
            deviceId = deviceId
        )
        return SignInResponse.from(signInVo).succeed()
    }

    @Operation(
        summary = "로그아웃",
        description = """
            ### 로그아웃
            
            - 클라이언트에서 사용하던 Access Token과 Refresh Token이 서버 측에서 파기됩니다.
            
            - 파기된 토큰은 재사용이 불가능하고, 새롭게 로그인을 하여 발급받아야 합니다.
        """,
    )
    @PostMapping("/sign-out")
    fun signOut(
        @Parameter(hidden = true) @RequestHeader(name = AUTH_JWT_HEADER, required = true) bearerAccessToken: String,
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
    ): CaramelApiResponse<Unit> {
        authService.signOut(
            bearerAccessToken = bearerAccessToken,
            deviceId = deviceId,
            userId = getCurrentUserId(),
        )
        return CaramelApiResponse.succeed()
    }

    @DisableSwaggerAuthButton
    @Operation(
        summary = "토큰 refresh",
        description = """
            ### 새로운 Access Token, Refresh Token을 재발급합니다.
            
            - 전송한 Access Token과 Refresh Token이 서버 측에서 파기됩니다.
            
            - 해당 요청 이후에는 새롭게 발급된 토큰을 사용해야 합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "발급 완료"),
            ApiResponse(responseCode = "403", description = "리프레시 토큰 만료. 재로그인 필요."),
        ]
    )
    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
        @RequestBody request: ServiceTokenResponse,
    ): CaramelApiResponse<ServiceTokenResponse> {
        val serviceTokenVo = authService.refresh(
            accessToken = request.accessToken,
            refreshToken = request.refreshToken,
            deviceId = deviceId,
        )
        return ServiceTokenResponse.from(serviceTokenVo).succeed()
    }

    @Operation(
        summary = "회원 탈퇴",
        description = """
            ### 회원 탈퇴를 진행합니다.
            
            - 커플이 있다면 커플 탈퇴를 진행됩니다.
            
            - Kakao 회원일 경우 Kakao Application과 연결을 해제합니다.
            
            - 로그아웃을 진행합니다.
        """,
    )
    @DeleteMapping("/account")
    fun deleteUser(
        @Parameter(hidden = true) @RequestHeader(name = AUTH_JWT_HEADER, required = true) bearerAccessToken: String,
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
    ): CaramelApiResponse<Unit> {
        authService.deleteUser(
            bearerAccessToken,
            deviceId,
            userId = getCurrentUserId(),
        )
        return CaramelApiResponse.succeed()
    }
}
