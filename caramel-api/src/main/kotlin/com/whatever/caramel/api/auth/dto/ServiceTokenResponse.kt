package com.whatever.caramel.api.auth.dto

import com.whatever.domain.auth.vo.ServiceTokenVo
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
