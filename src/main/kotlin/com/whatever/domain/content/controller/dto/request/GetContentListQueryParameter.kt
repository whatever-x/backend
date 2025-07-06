package com.whatever.domain.content.controller.dto.request

import com.whatever.global.cursor.CursorRequest
import com.whatever.global.cursor.DescOrder
import com.whatever.global.cursor.Sortable
import com.whatever.global.cursor.Sortables
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
) : CursorRequest

enum class ContentListSortType : Sortables {
    ID_DESC {
        override val sortables: List<Sortable> = listOf(DescOrder("id"))
    };
}