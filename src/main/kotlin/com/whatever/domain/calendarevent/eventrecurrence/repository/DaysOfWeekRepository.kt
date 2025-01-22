package com.whatever.domain.calendarevent.eventrecurrence.repository

import com.whatever.domain.calendarevent.eventrecurrence.model.DaysOfWeek
import org.springframework.data.jpa.repository.JpaRepository

interface DaysOfWeekRepository : JpaRepository<DaysOfWeek, Long> {
}