package com.whatever.domain.calendarevent.specialday.repository

import com.whatever.domain.calendarevent.specialday.model.SpecialDay
import com.whatever.domain.calendarevent.specialday.model.SpecialDayType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface SpecialDayRepository : JpaRepository<SpecialDay, Long> {

    @Query("""
        select spd
        from SpecialDay spd
        where spd.locDate between :startDate and :endDate
          and spd.type = :type
          and spd.isHoliday = :isHoliday
          and spd.isDeleted = false
        order by spd.locDate
    """)
    fun findAllByTypeAndBetweenStartDateAndEndDate(
        type: SpecialDayType,
        startDate: LocalDate,
        endDate: LocalDate,
        isHoliday: Boolean = true,
    ): List<SpecialDay>
}