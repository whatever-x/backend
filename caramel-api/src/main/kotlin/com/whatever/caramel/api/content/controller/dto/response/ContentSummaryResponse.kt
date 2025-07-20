package com.whatever.caramel.api.content.controller.dto.response

import com.whatever.domain.content.vo.ContentSummaryVo
import com.whatever.domain.content.vo.ContentType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "콘텐츠 요약 응답 DTO")
data class ContentSummaryResponse(
    @Schema(description = "콘텐츠 id. contentType에 따라 메모 id, 일정 id")
    val id: Long,

    @Schema(description = "콘텐츠 타입 표시 (예: MEMO, SCHEDULE)")
    val contentType: ContentType,
) {
    companion object {
        fun from(contentSummaryVo: ContentSummaryVo): ContentSummaryResponse {
            return ContentSummaryResponse(
                id = contentSummaryVo.id,
                contentType = ContentType.valueOf(contentSummaryVo.contentType.name)
            )
        }
    }
}
