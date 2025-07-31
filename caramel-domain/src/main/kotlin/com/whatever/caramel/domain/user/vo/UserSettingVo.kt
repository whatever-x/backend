package com.whatever.caramel.domain.user.vo

import com.whatever.caramel.domain.user.model.UserSetting

data class UserSettingVo(
    val notificationEnabled: Boolean,
) {
    companion object {
        fun from(userSetting: UserSetting): UserSettingVo {
            return UserSettingVo(
                notificationEnabled = userSetting.notificationEnabled,
            )
        }
    }
} 
