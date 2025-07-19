package com.whatever.domain.balancegame.vo

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_OPTION_NOT_ENOUGH
import com.whatever.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import java.time.LocalDate

data class BalanceGameVo(
    val id: Long = 0L,

    val gameDate: LocalDate,

    val question: String,

    val options: List<BalanceGameOptionVo> = listOf(),
) {

    companion object {
        fun from(
            balanceGame: BalanceGame,
            balanceGameOptions: List<BalanceGameOption>
        ): BalanceGameVo {
            if (balanceGameOptions.size < 2) {
                throw BalanceGameIllegalStateException(
                    errorCode = GAME_OPTION_NOT_ENOUGH,
                    errorUi = ErrorUi.Toast("밸런스 게임의 선택지가 모두 등록되지 않았어요."),
                )
            }

            return BalanceGameVo(
                id = balanceGame.id,
                gameDate = balanceGame.gameDate,
                question = balanceGame.question,
                options = balanceGameOptions
                    .filter { !it.isDeleted }
                    .sortedBy { it.id }
                    .map { BalanceGameOptionVo.from(it) }
            )
        }
    }

}
