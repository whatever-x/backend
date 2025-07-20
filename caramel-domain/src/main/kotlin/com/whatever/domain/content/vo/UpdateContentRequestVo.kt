package com.whatever.domain.content.vo

import com.whatever.domain.calendarevent.vo.DateTimeInfoVo

data class UpdateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tagList: List<Long>,
    val dateTimeInfo: DateTimeInfoVo?,
)
