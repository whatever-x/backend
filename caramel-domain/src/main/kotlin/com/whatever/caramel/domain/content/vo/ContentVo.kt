package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.domain.content.model.Content

data class ContentVo(
    val id: Long,
    val contentDetail: ContentDetailVo,
    val type: ContentType,
    val contentAsignee: ContentAssignee,
) {
    companion object {
        fun from(content: Content): ContentVo {
            return ContentVo(
                id = content.id,
                contentDetail = ContentDetailVo.from(content.contentDetail),
                type = content.type,
                contentAsignee = content.contentAsignee
            )
        }
    }
} 
