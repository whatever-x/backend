package com.whatever.domain.content.tag.controller.dto.response

import com.whatever.domain.content.tag.model.Tag

data class TagDetailListResponse(
    val tagList: List<TagDetailDto>,
)

data class TagDetailDto(
    val id: Long,
    val label: String
) {
    companion object {
        fun from(tag: Tag): TagDetailDto {
            return TagDetailDto(
                id = tag.id,
                label = tag.label
            )
        }
    }
}
