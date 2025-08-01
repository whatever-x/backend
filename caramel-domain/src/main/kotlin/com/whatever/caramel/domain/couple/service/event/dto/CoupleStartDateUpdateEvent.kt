package com.whatever.caramel.domain.couple.service.event.dto

import java.time.LocalDate

data class CoupleStartDateUpdateEvent(
    val oldDate: LocalDate?,
    val newDate: LocalDate,
    val memberIds: Set<Long>,
)
