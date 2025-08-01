package com.whatever.caramel.domain.notification.service.event

import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
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

    /**
     * 해당 이벤트들은 반드시
     * scheduled notificatio이 배치로 적재된 이후에
     * 로직이 돌아야 함
     *
     * 배치와 합치고 해당 주석 제거
     */

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
}