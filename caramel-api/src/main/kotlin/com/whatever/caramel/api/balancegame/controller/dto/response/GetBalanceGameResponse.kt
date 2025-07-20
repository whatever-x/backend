package com.whatever.caramel.api.balancegame.controller.dto.response

import com.whatever.domain.balancegame.vo.BalanceGameOptionVo
import com.whatever.domain.balancegame.vo.BalanceGameVo
import com.whatever.domain.balancegame.vo.UserChoiceOptionVo
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
        fun from(
            gameVo: BalanceGameVo,
            myChoice: UserChoiceOptionVo?,
            partnerChoice: UserChoiceOptionVo?,
        ): GetBalanceGameResponse {
            val myChoiceOptionVo =
                myChoice?.let { gameVo.options.firstOrNull { it.id == myChoice.balanceGameOptionId } }
            val partnerChoiceOptionVo =
                partnerChoice?.let { gameVo.options.firstOrNull { it.id == partnerChoice.balanceGameOptionId } }
            return GetBalanceGameResponse(
                gameInfo = BalanceGameInfo.of(gameVo, gameVo.options),
                myChoice = myChoiceOptionVo?.let { OptionInfo.from(it) },
                partnerChoice = partnerChoiceOptionVo?.let { OptionInfo.from(it) }
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
        fun of(
            game: BalanceGameVo,
            options: List<BalanceGameOptionVo>
        ): BalanceGameInfo {
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
        fun from(option: BalanceGameOptionVo): OptionInfo {
            return OptionInfo(
                id = option.id,
                text = option.optionText
            )
        }
    }
}
