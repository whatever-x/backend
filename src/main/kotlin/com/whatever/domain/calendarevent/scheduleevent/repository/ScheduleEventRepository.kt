package com.whatever.domain.calendarevent.scheduleevent.repository

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ScheduleEventRepository : JpaRepository<ScheduleEvent, Long> {
    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
            join fetch c.user
        where se.id = :scheduleId
    """)
    fun findByIdWithContentAndUser(scheduleId: Long): ScheduleEvent?
}