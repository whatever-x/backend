package com.whatever.caramel.infrastructure.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(SnakeCaseStrategy::class)
data class KakaoUserInfoResponse(
    val id: Long,
    val kakaoAccount: KakaoAccount,
    val properties: KakaoUserProperties? = null,
) {
    val platformUserId
        get() = id.toString()
}

@JsonNaming(SnakeCaseStrategy::class)
data class KakaoAccount(
    val profile: Profile,

    // 하위 요소들은 kakao biz 필요
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val birthYear: String? = null,
    val birthDay: String? = null,
)

@JsonNaming(SnakeCaseStrategy::class)
data class Profile(
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
)

@JsonNaming(SnakeCaseStrategy::class)
data class KakaoUserProperties(
    val nickname: String? = null,
)
