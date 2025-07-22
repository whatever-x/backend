package com.whatever.caramel.domain.content.tag.vo

import com.whatever.caramel.domain.content.tag.model.Tag

data class TagDetailVo(
    val id: Long,
    val label: String,
) {
    companion object {
        fun from(tag: Tag): TagDetailVo {
            return TagDetailVo(
                id = tag.id,
                label = tag.label
            )
        }
    }
} 
