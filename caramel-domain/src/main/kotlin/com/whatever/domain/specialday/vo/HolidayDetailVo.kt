package com.whatever.domain.specialday.vo

import com.whatever.domain.specialday.model.SpecialDay
import com.whatever.domain.specialday.model.SpecialDayType
import java.time.LocalDate

data class HolidayDetailVo(
    val id: Long,
    val type: SpecialDayType,
    val date: LocalDate,
    val name: String,
    val isHoliday: Boolean,
) {
    companion object {
        fun from(holiday: SpecialDay): HolidayDetailVo? {
            if (holiday.type != SpecialDayType.HOLI) {
                return null
            }
            return HolidayDetailVo(
                id = holiday.id,
                type = holiday.type,
                date = holiday.locDate,
                name = holiday.dateName,
                isHoliday = holiday.isHoliday,
            )
        }
    }
}
