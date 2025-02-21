package com.whatever.domain.calendarevent.timeevent.repository

import com.whatever.domain.calendarevent.timeevent.model.ScheduleEvent
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduleEventRepository : JpaRepository<ScheduleEvent, Long> {
}