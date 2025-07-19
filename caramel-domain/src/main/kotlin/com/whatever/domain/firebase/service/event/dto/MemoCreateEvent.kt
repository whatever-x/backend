package com.whatever.domain.firebase.service.event.dto

import com.whatever.domain.content.model.ContentDetail

data class MemoCreateEvent(
    val userId: Long,
    val coupleId: Long,
    val memberIds: Set<Long>,
    val contentDetail: ContentDetail,
)
