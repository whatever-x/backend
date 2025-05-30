package com.whatever.domain.user.dto

data class PatchUserSettingRequest(
    val notificationEnabled: Boolean? = null,
)
