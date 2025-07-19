package com.whatever.domain.content.vo

import com.whatever.domain.content.vo.ContentType

data class ContentSummaryVo(
    val contentId: Long,
    val contentType: ContentType
) {
    companion object {
        fun from(contentVo: ContentVo): ContentSummaryVo {
            return ContentSummaryVo(
                contentId = contentVo.id,
                contentType = contentVo.type
            )
        }
    }
} 