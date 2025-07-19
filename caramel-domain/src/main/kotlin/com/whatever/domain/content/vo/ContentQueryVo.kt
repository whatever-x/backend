package com.whatever.domain.content.vo

import com.whatever.caramel.common.global.cursor.CursorRequest
import com.whatever.caramel.common.global.cursor.DescOrder
import com.whatever.caramel.common.global.cursor.Sortable
import com.whatever.caramel.common.global.cursor.Sortables

data class ContentQueryVo(
    override val size: Int,
    override val cursor: String?,
    override val sortType: ContentListSortType,
    val tagId: Long?
) : CursorRequest

enum class ContentListSortType : Sortables {
    ID_DESC {
        override val sortables: List<Sortable> = listOf(DescOrder("id"))
    };
}