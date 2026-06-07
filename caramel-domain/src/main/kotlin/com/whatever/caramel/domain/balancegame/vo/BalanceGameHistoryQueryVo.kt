package com.whatever.caramel.domain.balancegame.vo

import com.whatever.caramel.common.global.cursor.CursorRequest
import com.whatever.caramel.common.global.cursor.DescOrder
import com.whatever.caramel.common.global.cursor.Sortable
import com.whatever.caramel.common.global.cursor.Sortables
import com.whatever.caramel.common.util.CursorUtil
import java.time.LocalDate

data class BalanceGameHistoryQueryVo(
    override val size: Int,
    override val cursor: String?,
    override val sortType: Sortables,
) : CursorRequest {
    /**
     * Base64로 인코딩된 커서를 gameDate(LocalDate)로 디코딩한다.
     * 첫 페이지 요청(cursor == null/blank)이면 null을 반환한다.
     */
    fun cursorDate(): LocalDate? {
        return cursor?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(CursorUtil.fromHash(it).first()) }
    }
}

enum class BalanceGameHistorySortType : Sortables {
    GAME_DATE_DESC {
        override val sortables: List<Sortable> = listOf(DescOrder("gameDate"))
    };
}
