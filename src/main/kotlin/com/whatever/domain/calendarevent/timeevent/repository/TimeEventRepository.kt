package com.whatever.domain.calendarevent.timeevent.repository

import com.whatever.domain.calendarevent.timeevent.model.TimeEvent
import org.springframework.data.jpa.repository.JpaRepository

interface TimeEventRepository : JpaRepository<TimeEvent, Long> {
}