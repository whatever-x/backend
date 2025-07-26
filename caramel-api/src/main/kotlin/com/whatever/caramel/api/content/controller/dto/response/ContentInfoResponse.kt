package com.whatever.caramel.api.content.controller.dto.response

import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.content.vo.ContentResponseVo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "메모 응답 DTO")
data class ContentResponse(
    @Schema(description = "메모 id")
    val id: Long,
    @Schema(description = "제목")
    val title: String,
    @Schema(description = "본문")
    val description: String,
    @Schema(description = "완료 여부")
    val isCompleted: Boolean,
    @Schema(description = "연관 태그 Id 리스트")
    val tagList: List<TagDto>,
    @Schema(description = "생성일")
    val createdAt: LocalDate,
    @Schema(description = "컨텐츠 담당자 (ME: 나, PARTNER: 상대방, US: 우리)")
    val contentAsignee: ContentAssignee,
) {
    companion object {
        fun from(
            content: Content,
            tagList: List<TagDto>,
        ) = ContentResponse(
            id = content.id,
            title = content.contentDetail.title ?: "",
            description = content.contentDetail.description ?: "",
            isCompleted = content.contentDetail.isCompleted,
            tagList = tagList,
            createdAt = content.getCreatedAtInZone(KST_ZONE_ID).toLocalDate(),
            contentAsignee = content.contentAsignee
        )

        fun from(contentResponseVo: ContentResponseVo): ContentResponse {
            return ContentResponse(
                id = contentResponseVo.id,
                title = contentResponseVo.title,
                description = contentResponseVo.description,
                isCompleted = contentResponseVo.isCompleted,
                tagList = contentResponseVo.tagList.map { TagDto.from(it) },
                createdAt = contentResponseVo.createdAt,
                contentAsignee = contentResponseVo.contentAsignee
            )
        }
    }
}
