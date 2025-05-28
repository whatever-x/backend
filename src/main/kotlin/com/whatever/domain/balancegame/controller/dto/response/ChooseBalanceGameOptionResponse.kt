package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption

data class ChooseBalanceGameOptionResponse(
    val gameInfo: BalanceGameInfo,
    val myChoice: OptionInfo?,
    val partnerChoice: OptionInfo?,
) {
    companion object {
        fun of(
            game: BalanceGame,
            options: List<BalanceGameOption>,
            myChoice: BalanceGameOption?,
            partnerChoice: BalanceGameOption?,
        ): ChooseBalanceGameOptionResponse {
            return ChooseBalanceGameOptionResponse(
                gameInfo = BalanceGameInfo.of(game, options),
                myChoice = myChoice?.let { OptionInfo.from(it) },
                partnerChoice = partnerChoice?.let { OptionInfo.from(it) }
            )
        }
    }
}