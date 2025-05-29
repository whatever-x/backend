package com.whatever.domain.user.dto

import com.whatever.domain.user.model.UserSetting

data class PatchUserSettingResponse(
    val notificationEnabled: Boolean,
) {
    companion object {
        fun from(userSetting: UserSetting): PatchUserSettingResponse {
            return PatchUserSettingResponse(
                notificationEnabled = userSetting.notificationEnabled,
            )
        }
    }
}