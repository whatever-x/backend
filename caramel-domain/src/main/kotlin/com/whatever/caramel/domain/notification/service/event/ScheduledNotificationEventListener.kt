package com.whatever.caramel.domain.notification.service.event

import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
import com.whatever.caramel.domain.firebase.service.event.dto.CoupleConnectedEvent
import com.whatever.caramel.domain.notification.service.event.handler.AnniversaryUpdatedEventHandler
import com.whatever.caramel.domain.user.service.event.dto.UserBirthDateUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {  }

@Component
class ScheduledNotificationEventListener(
    private val anniversaryUpdatedEventHandler: AnniversaryUpdatedEventHandler
) {
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleCoupleStartDateNotification(event: CoupleStartDateUpdateEvent) {
        anniversaryUpdatedEventHandler.handle(event)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleUserBirthDateNotification(event: UserBirthDateUpdateEvent) {
        anniversaryUpdatedEventHandler.handle(event)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleUserBirthDateNotification(event: CoupleConnectedEvent) {
        event.members.forEach { member ->
            val birthDateUpdateEvent = UserBirthDateUpdateEvent(
                userId = member.id,
                userNickname = member.nickname,
                oldDate = null,
                newDate = member.birthDate,
                coupleId = event.coupleDetailVo.id
            )
            anniversaryUpdatedEventHandler.handle(birthDateUpdateEvent)
        }
    }
}