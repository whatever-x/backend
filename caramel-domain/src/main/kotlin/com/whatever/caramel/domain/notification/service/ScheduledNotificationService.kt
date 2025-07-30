package com.whatever.caramel.domain.notification.service

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduledNotificationService(
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    fun scheduleNotification(
        targetUserId: Long,
        notificationType: NotificationType,
        notifyAt: LocalDateTime,
        title: String,
        body: String,
        image: String? = null,
    ) {
        val newNotification = ScheduledNotification(
            targetUserId = targetUserId,
            notificationType = notificationType,
            notifyAt = notifyAt,
            title = title,
            body = body,
            image = image
        )

        scheduledNotificationRepository.save(newNotification)
    }

    fun deleteScheduledNotifications(
        targetUserIds: Set<Long>,
        notificationTypes: Set<NotificationType>,
    ): Int {
        return scheduledNotificationRepository.deleteAllByNotificationTypeInAndTargetUserIdIn(
            notificationTypes = notificationTypes,
            targetUserIds = targetUserIds
        )
    }
}