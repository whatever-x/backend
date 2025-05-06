package com.whatever.domain.calendarevent.scheduleevent.service.event

import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {  }

@Component
class ScheduleEventListener(
    private val schedulerEventRepository: ScheduleEventRepository,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    fun deleteAllSchedule(event: CoupleMemberLeaveEvent) {
        val effectedRow = schedulerEventRepository.softDeleteAllByUserIdInBulk(event.userId)
        logger.info { "${effectedRow} delete Schedule" }
    }
}