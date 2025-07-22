package com.whatever.caramel.api.content.tag.controller.dto.response

import com.whatever.caramel.domain.content.tag.vo.TagDetailVo
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
    val label: String,
) {
    companion object {
        fun from(tagDetailVo: TagDetailVo): TagDetailDto {
            return TagDetailDto(
                id = tagDetailVo.id,
                label = tagDetailVo.label
            )
        }
    }
}
