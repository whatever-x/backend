package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption

data class ChooseBalanceGameOptionResponse(
    val gameId: Long,
    val myChoice: OptionInfo?,
    val partnerChoice: OptionInfo?,
) {
    companion object {
        fun of(
            game: BalanceGame,
            myChoice: BalanceGameOption?,
            partnerChoice: BalanceGameOption?,
        ): ChooseBalanceGameOptionResponse {
            return ChooseBalanceGameOptionResponse(
                gameId = game.id,
                myChoice = myChoice?.let { OptionInfo.from(it) },
                partnerChoice = partnerChoice?.let { OptionInfo.from(it) }
            )
        }
    }
}