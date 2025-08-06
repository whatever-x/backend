package com.whatever.caramel.domain.notification.vo

import com.whatever.caramel.domain.notification.model.NotificationType

data class NotificationMessageVo(
    val type: NotificationType,
    val title: String,
    val body: String,
)