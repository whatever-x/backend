package com.whatever.domain.content.vo

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.vo.ContentType

data class ContentVo(
    val id: Long,
    val contentDetail: ContentDetailVo,
    val type: ContentType
) {
    companion object {
        fun from(content: Content): ContentVo {
            return ContentVo(
                id = content.id,
                contentDetail = ContentDetailVo.from(content.contentDetail),
                type = content.type
            )
        }
    }
} 