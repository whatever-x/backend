package com.whatever.domain.couple.controller.dto.request

import java.time.LocalDate

data class UpdateCoupleStartDateRequest(
    val startDate: LocalDate
)

data class UpdateCoupleSharedMessageRequest(
    val sharedMessage: String?,
)