package com.whatever.caramel.domain.notification.service.event

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
import com.whatever.caramel.domain.firebase.service.event.dto.CoupleConnectedEvent
import com.whatever.caramel.domain.notification.exception.NotificationException
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
        try {
            anniversaryUpdatedEventHandler.handle(
                event = event,
                targetDate = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate(),
            )
        } catch (e: Exception) {
            logger.error {
                "Filed to handle couple start date update event. " +
                "Received event: ${event}. " +
                "Additional info: ${if (e is NotificationException) e.message else null}"
            }
        }
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleUserBirthDateNotification(event: UserBirthDateUpdateEvent) {
        try {
            anniversaryUpdatedEventHandler.handle(
                event = event,
                targetDate = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate(),
            )
        } catch (e: Exception) {
            logger.error {
                "Filed to handle user birthday update event. " +
                "Received event: ${event}. " +
                "Additional info: ${if (e is NotificationException) e.message else null}"
            }
        }
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleUserBirthDateNotification(event: CoupleConnectedEvent) {
        try {
            event.members.forEach { member ->
                val birthDateUpdateEvent = UserBirthDateUpdateEvent(
                    userId = member.id,
                    userNickname = member.nickname,
                    oldDate = null,
                    newDate = member.birthDate,
                    coupleId = event.coupleDetailVo.id
                )
                anniversaryUpdatedEventHandler.handle(
                    event = birthDateUpdateEvent,
                    targetDate = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate(),
                )
            }
        } catch (e: Exception) {
            logger.error {
                "Filed to handle user birthday update event after couple connected. " +
                "Received event: ${event}. " +
                "Additional info: ${if (e is NotificationException) e.message else null}"
            }
        }
    }
}