package com.whatever.domain.content.vo

import com.whatever.domain.content.tag.vo.TagIdVo

data class UpdateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tagList: List<TagIdVo>,
    val dateTimeInfo: DateTimeInfoVo?,
)
