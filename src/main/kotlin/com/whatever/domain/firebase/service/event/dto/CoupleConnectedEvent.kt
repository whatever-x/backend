package com.whatever.domain.firebase.service.event.dto

data class CoupleConnectedEvent(
    val coupleId: Long,
    val memberIds: Set<Long>,
)