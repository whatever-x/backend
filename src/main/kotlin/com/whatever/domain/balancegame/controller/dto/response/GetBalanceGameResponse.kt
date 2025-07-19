package com.whatever.domain.balancegame.controller.dto.response

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "밸런스 게임 응답 DTO")
data class GetBalanceGameResponse(
    @Schema(description = "밸런스 게임 정보")
    val gameInfo: BalanceGameInfo,

    @Schema(description = "내 선택 정보", nullable = true)
    val myChoice: OptionInfo?,

    @Schema(description = "상대방 선택 정보", nullable = true)
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

@Schema(description = "밸런스 게임 정보 DTO")
data class BalanceGameInfo(
    @Schema(description = "밸런스 게임 id")
    val id: Long,

    @Schema(description = "해당 날짜")
    val date: LocalDate,

    @Schema(description = "질문")
    val question: String,

    @Schema(description = "선택지 리스트")
    val options: List<OptionInfo>,
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

@Schema(description = "밸런스 게임 선택지 DTO")
data class OptionInfo(
    @Schema(description = "선택지 id")
    val id: Long,

    @Schema(description = "선택지 내용")
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
