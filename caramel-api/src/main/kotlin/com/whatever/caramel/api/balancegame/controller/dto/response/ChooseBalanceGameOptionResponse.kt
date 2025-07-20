package com.whatever.caramel.api.balancegame.controller.dto.response

import com.whatever.domain.balancegame.vo.BalanceGameVo
import com.whatever.domain.balancegame.vo.UserChoiceOptionVo

data class ChooseBalanceGameOptionResponse(
    val gameInfo: BalanceGameInfo,
    val myChoice: OptionInfo?,
    val partnerChoice: OptionInfo?,
) {
    companion object {
        fun from(
            gameVo: BalanceGameVo,
            myChoice: UserChoiceOptionVo?,
            partnerChoice: UserChoiceOptionVo?,
        ): ChooseBalanceGameOptionResponse {
            val myChoiceOptionVo =
                myChoice?.let { gameVo.options.firstOrNull { it.id == myChoice.balanceGameOptionId } }
            val partnerChoiceOptionVo =
                partnerChoice?.let { gameVo.options.firstOrNull { it.id == partnerChoice.balanceGameOptionId } }
            return ChooseBalanceGameOptionResponse(
                gameInfo = BalanceGameInfo.of(gameVo, gameVo.options),
                myChoice = myChoiceOptionVo?.let { OptionInfo.from(it) },
                partnerChoice = partnerChoiceOptionVo?.let { OptionInfo.from(it) }
            )
        }
    }
}
