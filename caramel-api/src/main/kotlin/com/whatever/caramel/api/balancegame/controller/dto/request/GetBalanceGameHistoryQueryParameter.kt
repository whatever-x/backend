package com.whatever.caramel.api.balancegame.controller.dto.request

import com.whatever.caramel.common.global.cursor.CursorRequest
import com.whatever.caramel.domain.balancegame.vo.BalanceGameHistoryQueryVo
import com.whatever.caramel.domain.balancegame.vo.BalanceGameHistorySortType
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class GetBalanceGameHistoryQueryParameter(
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
    override val sortType: BalanceGameHistorySortType = BalanceGameHistorySortType.GAME_DATE_DESC
) : CursorRequest {
    fun toVo(): BalanceGameHistoryQueryVo {
        return BalanceGameHistoryQueryVo(
            size = size,
            cursor = cursor,
            sortType = sortType,
        )
    }
}
