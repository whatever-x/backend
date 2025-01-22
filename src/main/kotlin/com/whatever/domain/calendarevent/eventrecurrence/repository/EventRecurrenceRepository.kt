package com.whatever.domain.calendarevent.eventrecurrence.repository

import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import org.springframework.data.jpa.repository.JpaRepository

interface EventRecurrenceRepository : JpaRepository<EventRecurrence, Long> {
}