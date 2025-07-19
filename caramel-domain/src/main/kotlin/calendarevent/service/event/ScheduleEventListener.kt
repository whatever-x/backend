package com.whatever.calendarevent.service.event

import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger { }

@Component
class ScheduleEventListener(
    private val scheduleEventCleanupService: ScheduleEventCleanupService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun deleteAllSchedule(event: CoupleMemberLeaveEvent) {
        scheduleEventCleanupService.cleanupEntity(
            userId = event.userId,
            entityName = ENTITY_NAME
        )
    }

    companion object {
        const val ENTITY_NAME = "Schedule"
    }
}
