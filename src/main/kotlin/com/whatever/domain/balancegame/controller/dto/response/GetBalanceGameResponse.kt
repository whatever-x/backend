package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.user.exception.UserExceptionCode
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.global.exception.GlobalException
import com.whatever.global.exception.GlobalExceptionCode
import java.time.LocalDate

data class GetBalanceGameResponse(
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
        ): GetBalanceGameResponse {
            return GetBalanceGameResponse(
                gameInfo = BalanceGameInfo.of(game, options),
                myChoice = myChoice?.let { OptionInfo.from(it) },
                partnerChoice = partnerChoice?.let { OptionInfo.from(it) }
            )
        }
    }
}

data class BalanceGameInfo(
    val id: Long,
    val date: LocalDate,
    val question: String,
    val options: List<OptionInfo>
) {
    companion object {
        fun of(game: BalanceGame, options: List<BalanceGameOption>): BalanceGameInfo {
            return BalanceGameInfo(
                id = game.id,
                date = game.gameDate,
                question = game.question,
                options = options.map { OptionInfo.from(it) }
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

data class UserChoiceInfo(
    val optionId: Long,
    val text: String,
) {
    companion object {
        fun from(option: BalanceGameOption): UserChoiceInfo {
            return UserChoiceInfo(
                optionId = option.id,
                text = option.optionText
            )
        }
    }
}