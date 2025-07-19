package com.whatever.domain.content.vo

import com.whatever.domain.content.tag.vo.TagIdVo

data class CreateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tags: List<TagIdVo>
)