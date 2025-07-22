package com.whatever.caramel.domain.content.vo

import com.whatever.caramel.domain.content.model.ContentDetail

data class ContentDetailVo(
    val title: String?,
    val description: String?,
    val isCompleted: Boolean,
) {
    companion object {
        fun from(contentDetail: ContentDetail): ContentDetailVo {
            return ContentDetailVo(
                title = contentDetail.title,
                description = contentDetail.description,
                isCompleted = contentDetail.isCompleted
            )
        }
    }
}
