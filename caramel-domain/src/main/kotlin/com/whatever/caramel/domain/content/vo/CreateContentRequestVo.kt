package com.whatever.caramel.domain.content.vo

data class CreateContentRequestVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
    val tags: List<Long>,
    val ownerType: ContentOwnerType,
)
