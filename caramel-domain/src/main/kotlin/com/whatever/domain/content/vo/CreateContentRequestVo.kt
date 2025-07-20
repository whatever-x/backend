package com.whatever.domain.content.vo

data class CreateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tags: List<Long>,
)
