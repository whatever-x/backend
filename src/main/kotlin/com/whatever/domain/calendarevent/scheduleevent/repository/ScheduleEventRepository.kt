package com.whatever.domain.calendarevent.scheduleevent.repository

import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduleEventRepository : JpaRepository<ScheduleEvent, Long> {
}