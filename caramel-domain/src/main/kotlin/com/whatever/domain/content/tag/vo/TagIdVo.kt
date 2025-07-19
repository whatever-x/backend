package com.whatever.domain.content.tag.vo

import com.whatever.domain.content.controller.dto.request.TagIdDto

data class TagIdVo(
    val tagId: Long,
) {
    companion object {
        fun from(tagIdDto: TagIdDto): TagIdVo {
            return TagIdVo(
                tagId = tagIdDto.tagId
            )
        }
    }
} 
