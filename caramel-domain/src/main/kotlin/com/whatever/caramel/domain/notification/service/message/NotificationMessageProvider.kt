package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessage
import org.springframework.stereotype.Component

@Component
class NotificationMessageProvider(
    generators: List<NotificationMessageGenerator>
) {
    private val generatorMap = generators.associateBy { it.supports() }

    fun provide(type: NotificationType, info: NotificationInformation): NotificationMessage {
        val generator = generatorMap[type] ?: throw RuntimeException()  // TODO CustomException
        return generator.generate(info)

    }
}
