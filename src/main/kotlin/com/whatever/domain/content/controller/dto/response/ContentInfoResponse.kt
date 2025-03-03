package com.whatever.domain.content.controller.dto.response

import com.whatever.domain.content.model.ContentType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "콘텐츠 리스트 응답 모델")
data class ContentDetailListResponse(
    val contentList: List<ContentDetailResponse>
)

@Schema(description = "콘텐츠 상세 응답 모델")
data class ContentDetailResponse(
    val contentId: Long,
    val title: String,
    val description: String,
    val wishDate: LocalDate?,
    val isCompleted: Boolean,
    val tagList: List<TagDto> = emptyList()
)

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