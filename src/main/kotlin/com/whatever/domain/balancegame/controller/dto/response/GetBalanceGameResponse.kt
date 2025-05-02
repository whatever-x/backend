package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import java.time.LocalDate

data class GetBalanceGameResponse(
    val gameInfo: BalanceGameInfo,
    val options: List<OptionInfo>,
) {
    companion object {
        fun of(
            game: BalanceGame,
            options: List<BalanceGameOption>
        ): GetBalanceGameResponse {
            return GetBalanceGameResponse(
                gameInfo = BalanceGameInfo.from(game),
                options = options.map { OptionInfo.from(it) }
            )
        }
    }
}

data class BalanceGameInfo(
    val id: Long,
    val date: LocalDate,
    val question: String,
) {
    companion object {
        fun from(game: BalanceGame): BalanceGameInfo {
            return BalanceGameInfo(
                id = game.id,
                date = game.gameDate,
                question = game.question
            )
        }
    }
}

data class OptionInfo(
    val id: Long,
    val text: String,
) {
    companion object {
        fun from(option: BalanceGameOption): OptionInfo {
            return OptionInfo(
                id = option.id,
                text = option.optionText
            )
        }
    }
}