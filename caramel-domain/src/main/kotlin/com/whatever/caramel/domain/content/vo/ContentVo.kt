package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.domain.content.model.Content

data class ContentVo(
    val id: Long,
    val contentDetail: ContentDetailVo,
    val type: ContentType,
    val ownerType: ContentOwnerType,
) {
    companion object {
        fun from(content: Content): ContentVo {
            return ContentVo(
                id = content.id,
                contentDetail = ContentDetailVo.from(content.contentDetail),
                type = content.type,
                ownerType = content.ownerType
            )
        }
    }
} 
