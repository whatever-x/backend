package com.whatever.caramel.domain.calendarevent.service.event

import com.whatever.caramel.domain.base.AbstractEntityCleanupService
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import org.springframework.stereotype.Service

@Service
class ScheduleEventCleanupService(
    private val scheduleEventRepository: ScheduleEventRepository,
) : AbstractEntityCleanupService<ScheduleEvent>() {

    override fun runCleanup(userId: Long): Int {
        return scheduleEventRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
