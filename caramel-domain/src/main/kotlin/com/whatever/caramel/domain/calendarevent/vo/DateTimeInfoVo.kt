package com.whatever.caramel.domain.calendarevent.vo

import java.time.LocalDateTime

data class DateTimeInfoVo(
    val startDateTime: LocalDateTime,
    val startTimezone: String,
    val endDateTime: LocalDateTime? = null,
    val endTimezone: String? = null,
) {
    companion object {
        fun from(
            startDateTime: LocalDateTime,
            startTimezone: String,
            endDateTime: LocalDateTime?,
            endTimezone: String?,
        ): DateTimeInfoVo {
            return DateTimeInfoVo(
                startDateTime = startDateTime,
                startTimezone = startTimezone,
                endDateTime = endDateTime,
                endTimezone = endTimezone,
            )
        }
    }
}
