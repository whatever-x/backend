package com.whatever.domain.content.vo

data class ContentSummaryVo(
    val id: Long,
    val contentType: ContentType,
) {
    companion object {
        fun from(contentVo: ContentVo): ContentSummaryVo {
            return ContentSummaryVo(
                id = contentVo.id,
                contentType = contentVo.type
            )
        }
    }
} 
