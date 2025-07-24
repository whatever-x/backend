package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo

data class UpdateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tagList: List<Long>,
    val dateTimeInfo: DateTimeInfoVo?,
    val ownerType: ContentOwnerType,
)
