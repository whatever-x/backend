package com.whatever.caramel.domain.firebase.service.event.dto

data class BalanceGameOptionChosenEvent(
    val userId: Long,
    val memberIds: Set<Long>,
)
