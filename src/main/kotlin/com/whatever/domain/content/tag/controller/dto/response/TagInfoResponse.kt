package com.whatever.domain.content.tag.controller.dto.response

import com.whatever.domain.content.tag.model.Tag
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "태그 상세정보 리스트 응답 DTO")
data class TagDetailListResponse(
    val tagList: List<TagDetailDto>,
)

@Schema(description = "태그 상세정보 DTO")
data class TagDetailDto(
    @Schema(description = "태그 id")
    val id: Long,
    @Schema(description = "태그 라벨")
    val label: String
) {
    companion object {
        fun from(tag: Tag): TagDetailDto {
            return TagDetailDto(
                id = tag.id,
                label = tag.label
            )
        }
    }
}
