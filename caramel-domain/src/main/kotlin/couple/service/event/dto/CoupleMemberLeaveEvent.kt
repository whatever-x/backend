package com.whatever.couple.service.event.dto

data class CoupleMemberLeaveEvent(
    val coupleId: Long,
    val userId: Long,
)
