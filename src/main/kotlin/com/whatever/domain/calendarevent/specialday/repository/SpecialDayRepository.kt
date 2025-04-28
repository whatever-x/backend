package com.whatever.domain.calendarevent.specialday.repository

import com.whatever.domain.calendarevent.specialday.model.SpecialDay
import org.springframework.data.jpa.repository.JpaRepository

interface SpecialDayRepository : JpaRepository<SpecialDay, Long> {
}