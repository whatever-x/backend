package com.whatever.domain.firebase.service.event.dto

import com.whatever.domain.content.model.ContentDetail

data class ScheduleCreateEvent(
    val userId: Long,
    val coupleId: Long,
    val memberIds: Set<Long>,
    val contentDetail: ContentDetail,
)
