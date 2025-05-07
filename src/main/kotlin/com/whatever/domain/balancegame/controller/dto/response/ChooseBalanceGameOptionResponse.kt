package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption

data class ChooseBalanceGameOptionResponse(
    val gameId: Long,
    val myChoice: UserChoiceInfo?,
    val partnerChoice: UserChoiceInfo?,
) {
    companion object {
        fun of(
            game: BalanceGame,
            myChoice: UserChoiceOption?,
            partnerChoice: UserChoiceOption?,
        ): ChooseBalanceGameOptionResponse {
            return ChooseBalanceGameOptionResponse(
                gameId = game.id,
                myChoice = myChoice?.let { UserChoiceInfo.from(it) },
                partnerChoice = partnerChoice?.let { UserChoiceInfo.from(it) }
            )
        }
    }
}