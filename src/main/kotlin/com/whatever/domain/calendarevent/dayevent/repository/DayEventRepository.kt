package com.whatever.domain.calendarevent.dayevent.repository

import com.whatever.domain.calendarevent.dayevent.model.DayEvent
import org.springframework.data.jpa.repository.JpaRepository

interface DayEventRepository : JpaRepository<DayEvent, Long> {
}