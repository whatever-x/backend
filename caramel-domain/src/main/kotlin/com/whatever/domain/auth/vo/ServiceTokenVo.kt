package com.whatever.domain.auth.vo

data class ServiceTokenVo(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun from(accessToken: String, refreshToken: String): ServiceTokenVo {
            return ServiceTokenVo(
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        }
    }
}
