package com.whatever.domain.calendarevent.eventrecurrence.repository

import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrenceDayMapping
import org.springframework.data.jpa.repository.JpaRepository

interface EventRecurrenceDayMappingRepository : JpaRepository<EventRecurrenceDayMapping, Long> {
}