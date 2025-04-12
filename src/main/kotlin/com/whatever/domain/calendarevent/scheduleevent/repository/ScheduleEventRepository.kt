package com.whatever.domain.calendarevent.scheduleevent.repository

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ScheduleEventRepository : JpaRepository<ScheduleEvent, Long> {
    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
            join fetch c.user u
        where se.id = :scheduleId
            and se.isDeleted = false 
    """)
    fun findByIdWithContentAndUser(scheduleId: Long): ScheduleEvent?

    @Query("""
        select se from ScheduleEvent se
            join fetch se.content c
        where se.id = :scheduleId
            and se.isDeleted = false 
    """)
    fun findByIdWithContent(scheduleId: Long): ScheduleEvent?

}