package com.whatever.caramel.domain.balancegame.vo

import com.whatever.caramel.domain.balancegame.model.BalanceGame
import com.whatever.caramel.domain.balancegame.model.BalanceGameOption
import com.whatever.caramel.domain.balancegame.model.UserChoiceOption

data class BalanceGameHistoryVo(
    val balanceGame: BalanceGameVo,
    val coupleChoiceOption: CoupleChoiceOptionVo,
) {
    companion object {
        fun from(
            balanceGame: BalanceGame,
            balanceGameOptions: List<BalanceGameOption>,
            myChoice: UserChoiceOption?,
            partnerChoice: UserChoiceOption?,
        ): BalanceGameHistoryVo {
            return BalanceGameHistoryVo(
                balanceGame = BalanceGameVo.from(
                    balanceGame = balanceGame,
                    balanceGameOptions = balanceGameOptions,
                ),
                coupleChoiceOption = CoupleChoiceOptionVo.from(
                    myChoice = myChoice?.let { UserChoiceOptionVo.from(it) },
                    partnerChoice = partnerChoice?.let { UserChoiceOptionVo.from(it) },
                ),
            )
        }
    }
}