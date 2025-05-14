package com.whatever.domain.content.controller.dto.response

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.tag.model.Tag
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메모 응답 모델")
data class ContentResponse(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val tagList: List<TagDto>,
) {
    companion object {
        fun from(
            content: Content,
            tagList: List<TagDto>
        ) = ContentResponse(
            id = content.id,
            title = content.contentDetail.title ?: "",
            description = content.contentDetail.description ?: "",
            isCompleted = content.contentDetail.isCompleted,
            tagList = tagList,
        )
    }
}

data class TagDto(
    val id: Long,
    val label: String
) {
    companion object {
        fun from(tag: Tag) = TagDto(
            id = tag.id,
            label = tag.label,
        )
    }
}

@Schema(description = "콘텐츠 요약 응답 모델")
data class ContentSummaryResponse(
    @Schema(description = "콘텐츠 id")
    val contentId: Long,

    @Schema(description = "콘텐츠 타입 표시 (예: MEMO, SCHEDULE)")
    val contentType: ContentType
)