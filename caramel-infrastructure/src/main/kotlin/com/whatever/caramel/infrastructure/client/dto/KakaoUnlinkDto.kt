package com.whatever.caramel.infrastructure.client.dto

import feign.form.FormProperty

data class KakaoUnlinkUserRequest(
    @FormProperty("target_id")
    private var targetId: Long,
) {
    @FormProperty("target_id_type")
    private var targetIdType = "user_id"  // kakao 스펙상 user_id로 고정
}

data class KakaoUnlinkUserResponse(
    val id: Long,
)
