package com.whatever.domain.calendarevent.vo

import com.whatever.domain.content.vo.ContentType

data class ContentSummaryVo(
    val contentId: Long,
    val contentType: ContentType,
) {
    companion object {
        fun from(contentId: Long, contentType: ContentType): ContentSummaryVo {
            return ContentSummaryVo(
                contentId = contentId,
                contentType = contentType,
            )
        }
    }
}
