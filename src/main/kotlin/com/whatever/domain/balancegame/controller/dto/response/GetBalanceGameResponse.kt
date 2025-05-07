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
    val options: List<OptionInfo>,
    val myChoice: UserChoiceInfo?,
    val partnerChoice: UserChoiceInfo?,
) {
    companion object {
        fun of(
            game: BalanceGame,
            options: List<BalanceGameOption>,
            myChoice: UserChoiceOption?,
            partnerChoice: UserChoiceOption?,
        ): GetBalanceGameResponse {
            return GetBalanceGameResponse(
                gameInfo = BalanceGameInfo.from(game),
                options = options.map { OptionInfo.from(it) },
                myChoice = myChoice?.let { UserChoiceInfo.from(it) },
                partnerChoice = partnerChoice?.let { UserChoiceInfo.from(it) }
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

data class UserChoiceInfo(
    val userId: Long,
    val nickname: String,
    val optionId: Long,
) {
    companion object {
        fun from(userChoice: UserChoiceOption): UserChoiceInfo {
            return UserChoiceInfo(
                userId = userChoice.user.id,
                nickname = userChoice.user.nickname
                    ?: throw GlobalException(
                        errorCode = GlobalExceptionCode.ILLEGAL_STATE,
                        detailMessage = "Illegal User Status. Nickname is null. user id: ${userChoice.user.id}"
                    ),
                optionId = userChoice.balanceGameOption.id,
            )
        }
    }
}