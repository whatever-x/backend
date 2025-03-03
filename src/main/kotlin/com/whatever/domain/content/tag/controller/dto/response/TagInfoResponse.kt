package com.whatever.domain.content.tag.controller.dto.response

data class TagDetailListResponse(
    val tagList: List<TagDetailDto>,
)

data class TagDetailDto(
    val tagId: Long,
    val tagLabel: String
)
