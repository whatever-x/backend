package com.whatever.domain.content.controller.dto.response

import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메모 목록 응답 모델")
data class ContentResponse(
    val id: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val tagList: List<TagDto> = emptyList()
) {
    companion object {
        fun from(content: Content) = ContentResponse(
            id = content.id,
            title = content.contentDetail.title ?: "",
            description = content.contentDetail.description ?: "",
            isCompleted = content.contentDetail.isCompleted,
            tagList = listOf()
        )
    }
}

data class TagDto(
    val tagId: Long,
    val tagLabel: String
)

@Schema(description = "콘텐츠 요약 응답 모델")
data class ContentSummaryResponse(
    @Schema(description = "콘텐츠 id")
    val contentId: Long,

    @Schema(description = "콘텐츠 타입 표시 (예: MEMO, SCHEDULE)")
    val contentType: ContentType
)