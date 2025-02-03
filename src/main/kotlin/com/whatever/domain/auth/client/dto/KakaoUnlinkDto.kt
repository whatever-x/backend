package com.whatever.domain.auth.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoUnlinkUser(
    val targetId: Long
) {
    val targetIdType = "user_id"  // kakao 스펙상 user_id로 고정
}

data class KakaoUnlinkUserResponse(
    val id: Long
)
