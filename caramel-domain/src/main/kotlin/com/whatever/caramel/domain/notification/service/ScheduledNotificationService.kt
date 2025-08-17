package com.whatever.caramel.domain.notification.service

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ScheduledNotificationService(
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    fun scheduleNotifications(
        messagesByUserId: Map<Long, NotificationMessageVo>,
        notifyAt: LocalDateTime,
        image: String? = null,
    ) {
        if (messagesByUserId.isEmpty()) {
            return
        }

        val notifications = messagesByUserId.map { (userId, message) ->
            ScheduledNotification(
                targetUserId = userId,
                notificationType = message.type,
                notifyAt = notifyAt,
                title = message.title,
                body = message.body,
                image = image,
            )
        }
        scheduledNotificationRepository.saveAll(notifications)
    }

    @Transactional(readOnly = true)
    fun getMatchedScheduledNotifications(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<ScheduledNotification> {
        return scheduledNotificationRepository.findByNotifyAt(startOfDay, endOfDay)
    }

    fun deleteScheduledNotifications(
        targetUserIds: Set<Long>,
        notificationTypes: Set<NotificationType>,
    ): Int {
        if (targetUserIds.isEmpty() || notificationTypes.isEmpty()) {
            return 0
        }

        return scheduledNotificationRepository.deleteAllByNotificationTypeInAndTargetUserIdIn(
            notificationTypes = notificationTypes,
            targetUserIds = targetUserIds
        )
    }
}
