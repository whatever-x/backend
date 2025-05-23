package com.whatever.domain.content.tag.controller.dto.response

import com.whatever.domain.content.tag.model.Tag

data class TagDetailListResponse(
    val tagList: List<TagDetailDto>,
)

data class TagDetailDto(
    val tagId: Long,
    val tagLabel: String
) {
    companion object {
        fun from(tag: Tag): TagDetailDto {
            return TagDetailDto(
                tagId = tag.id,
                tagLabel = tag.label
            )
        }
    }
}
