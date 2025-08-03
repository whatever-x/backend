package com.whatever.caramel.domain.notification.vo

data class NotificationVo(
    val targetUserId: Long,
    val title: String,
    val body: String,
    val image: String? = null,
)


data class NotificationMessage(
    val title: String,
    val body: String,
)