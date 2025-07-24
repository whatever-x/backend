package com.whatever.caramel.api.content.controller.dto.response

import com.whatever.caramel.domain.content.tag.vo.TagVo

data class TagDto(
    val id: Long,
    val label: String,
) {
    companion object {
        fun from(tagVo: TagVo) = TagDto(
            id = tagVo.id,
            label = tagVo.label,
        )
    }
}
