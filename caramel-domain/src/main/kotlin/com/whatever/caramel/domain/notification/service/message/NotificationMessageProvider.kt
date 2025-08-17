package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.exception.UnsupportedNotificationTypeException
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.springframework.stereotype.Component

@Component
class NotificationMessageProvider(
    generators: List<NotificationMessageGenerator>
) {
    private val generatorMap = generators.associateBy { it.supports() }

    fun provide(
        type: NotificationType,
        notificationMessageParameter: NotificationMessageParameter,
    ): NotificationMessageVo {
        val generator = generatorMap[type] ?: throw UnsupportedNotificationTypeException(
            detailMessage = "No notification generator found for NotificationType: ${type}"
        )
        return generator.generate(notificationMessageParameter)
    }
}
