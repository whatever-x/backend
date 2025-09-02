package com.whatever.caramel.domain.user.service.event.dto

import java.time.LocalDate

data class UserBirthDateUpdateEvent(
    val userId: Long,
    val userNickname: String,
    val oldDate: LocalDate?,
    val newDate: LocalDate,
    val coupleId: Long,
)
