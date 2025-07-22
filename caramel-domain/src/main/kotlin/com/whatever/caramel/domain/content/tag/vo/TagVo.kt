package com.whatever.caramel.domain.content.tag.vo

import com.whatever.caramel.domain.content.tag.model.Tag

data class TagVo(
    val id: Long,
    val label: String,
) {
    companion object {
        fun from(tag: Tag): TagVo {
            return TagVo(
                id = tag.id,
                label = tag.label
            )
        }
    }
} 
