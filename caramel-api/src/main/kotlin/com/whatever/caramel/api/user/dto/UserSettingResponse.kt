package com.whatever.caramel.api.user.dto

import com.whatever.domain.user.vo.UserSettingVo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "설정 정보 DTO")
data class UserSettingResponse(
    @Schema(description = "현재 알림 설정")
    val notificationEnabled: Boolean,
) {
    companion object {
        fun from(userSettingVo: UserSettingVo): UserSettingResponse {
            return UserSettingResponse(
                notificationEnabled = userSettingVo.notificationEnabled,
            )
        }
    }
}
