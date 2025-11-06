package com.whatever.caramel.api.auth.dto

import com.whatever.caramel.domain.auth.vo.ServiceTokenVo
import com.whatever.caramel.domain.auth.vo.TokenRefreshVo
import com.whatever.caramel.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "api 요청에 사용되는 JWT DTO")
data class ServiceTokenResponse(
    @Schema(description = "서버 Access Token 입니다. API 요청에 사용됩니다.")
    val accessToken: String,
    @Schema(description = "서버 Refresh Token 입니다. Access Token 갱신에 사용됩니다.")
    val refreshToken: String,
) {
    companion object {
        fun from(serviceTokenVo: ServiceTokenVo): ServiceTokenResponse {
            return ServiceTokenResponse(
                accessToken = serviceTokenVo.accessToken,
                refreshToken = serviceTokenVo.refreshToken,
            )
        }
    }
}

@Schema(description = "JWT 갱신 응답 DTO")
data class TokenRefreshResponse(
    @Schema(description = "서버에서 발급한 JWT(access, refresh) 정보")
    val serviceToken: ServiceTokenResponse,
    @Schema(description = "토큰 refresh 유저의 고유 ID")
    val userId: Long,
    @Schema(description = "유저의 현재 상태")
    val userStatus: UserStatus,
) {
    companion object {
        fun from(tokenRefreshVo: TokenRefreshVo): TokenRefreshResponse {
            return TokenRefreshResponse(
                serviceToken = ServiceTokenResponse.from(tokenRefreshVo.serviceTokenVo),
                userId = tokenRefreshVo.userId,
                userStatus = tokenRefreshVo.userStatus,
            )
        }
    }
}
