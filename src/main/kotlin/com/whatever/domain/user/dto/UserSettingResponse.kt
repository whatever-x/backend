package com.whatever.domain.user.dto

import com.whatever.domain.user.model.UserSetting

data class UserSettingResponse(
    val notificationEnabled: Boolean,
) {
    companion object {
        fun from(userSetting: UserSetting): UserSettingResponse {
            return UserSettingResponse(
                notificationEnabled = userSetting.notificationEnabled,
            )
        }
    }
}