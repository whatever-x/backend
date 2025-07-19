package com.whatever.domain.content.vo

import java.time.LocalDateTime

data class DateTimeInfoVo(
    val startDateTime: LocalDateTime,
    val startTimezone: String,
    val endDateTime: LocalDateTime?,
    val endTimezone: String?
)