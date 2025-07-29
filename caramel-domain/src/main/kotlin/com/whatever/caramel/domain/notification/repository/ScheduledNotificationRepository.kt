package com.whatever.caramel.domain.notification.repository

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ScheduledNotificationRepository : JpaRepository<ScheduledNotification, Long> {
    fun findAllByNotificationTypeAndTargetUserIdIn(
        notificationTypes: Set<NotificationType>,
        targetUserIds: Set<Long>
    ): List<ScheduledNotification>

    fun deleteAllByNotificationTypeInAndTargetUserIdIn(
        notificationTypes: Set<NotificationType>,
        targetUserIds: Set<Long>
    ): Long
}