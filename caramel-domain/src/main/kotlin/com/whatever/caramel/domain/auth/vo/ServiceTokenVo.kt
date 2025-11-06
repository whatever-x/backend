package com.whatever.caramel.domain.auth.vo

data class ServiceTokenVo(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
) {
    companion object {
        fun from(accessToken: String, refreshToken: String, userId: Long): ServiceTokenVo {
            return ServiceTokenVo(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
            )
        }
    }
}
