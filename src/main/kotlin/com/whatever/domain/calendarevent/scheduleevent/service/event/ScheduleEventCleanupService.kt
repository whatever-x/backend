package com.whatever.domain.calendarevent.scheduleevent.service.event

import com.whatever.domain.base.AbstractEntityCleanupService
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import org.springframework.stereotype.Service

@Service
class ScheduleEventCleanupService(
    private val scheduleEventRepository: ScheduleEventRepository
) : AbstractEntityCleanupService<ScheduleEvent>() {

    override fun runCleanup(userId: Long): Int {
        return scheduleEventRepository.softDeleteAllByUserIdInBulk(userId)
    }
}