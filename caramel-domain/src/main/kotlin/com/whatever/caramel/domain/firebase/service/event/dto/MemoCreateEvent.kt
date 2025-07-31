package com.whatever.caramel.domain.firebase.service.event.dto

import com.whatever.caramel.domain.content.vo.ContentDetailVo

data class MemoCreateEvent(
    val userId: Long,
    val coupleId: Long,
    val memberIds: Set<Long>,
    val contentDetail: ContentDetailVo,
)
