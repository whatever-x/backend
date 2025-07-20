package com.whatever.caramel.api.content.controller.dto.request

import com.whatever.caramel.common.global.cursor.CursorRequest
import com.whatever.domain.content.vo.ContentListSortType
import com.whatever.domain.content.vo.ContentQueryVo
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Schema(description = "커서 기반 메모 리스트 조회 DTO")
data class GetContentListQueryParameter(
    @field:Parameter(
        name = "size",
        description = "조회할 크기(5 ~ 30). 기본 30",
        `in` = ParameterIn.QUERY,
        required = false,
    )
    @field:Min(5)
    @field:Max(30)
    override val size: Int = 30,

    @field:Parameter(
        name = "cursor",
        description = "다음 페이지에 대한 커서",
        `in` = ParameterIn.QUERY,
        required = false,
    )
    override val cursor: String?,
    override val sortType: ContentListSortType = ContentListSortType.ID_DESC,
    val tagId: Long? = null,
) : CursorRequest {
    fun toVo(): ContentQueryVo {
        return ContentQueryVo(
            size = this.size,
            cursor = this.cursor,
            sortType = this.sortType,
            tagId = this.tagId
        )
    }
}

