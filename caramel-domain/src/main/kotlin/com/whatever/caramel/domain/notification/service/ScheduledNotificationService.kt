package com.whatever.caramel.domain.notification.service

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.domain.notification.vo.NotificationMessage
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduledNotificationService(
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    fun scheduleNotifications(
        messagesByUserId: Map<Long, NotificationMessage>,
        notificationType: NotificationType,
        notifyAt: LocalDateTime,
        image: String? = null,
    ) {
        if (messagesByUserId.isEmpty()) {
            return
        }

        val notifications = messagesByUserId.map { (userId, message) ->
            ScheduledNotification(
                targetUserId = userId,
                notificationType = notificationType,
                notifyAt = notifyAt,
                title = message.title,
                body = message.body,
                image = image,
            )
        }
        scheduledNotificationRepository.saveAll(notifications)
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