package com.whatever.domain.content.controller.dto.request

import com.whatever.global.cursor.CursorRequest
import com.whatever.global.cursor.DescOrder
import com.whatever.global.cursor.Sortable
import com.whatever.global.cursor.Sortables
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GetContentListQueryParameter(
    @field:Parameter(
        name = "pageSize",
        description = "조회할 크기(5 ~ 20). 기본 10",
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
) : CursorRequest

enum class ContentListSortType : Sortables {
    ID_DESC {
        override val sortables: List<Sortable> = listOf(DescOrder("id"))
    };
}