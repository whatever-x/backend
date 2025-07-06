package com.whatever.domain.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유저 설정 수정 요청 DTO")
data class PatchUserSettingRequest(
    @Schema(description = "알림 설정 여부. 설정하지 않으려면 null, 혹은 추가하지 않고 요청 전송", nullable = true)
    val notificationEnabled: Boolean? = null,
)
