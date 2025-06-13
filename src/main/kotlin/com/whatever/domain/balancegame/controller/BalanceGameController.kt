package com.whatever.domain.balancegame.controller

import com.whatever.domain.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.domain.balancegame.controller.dto.response.ChooseBalanceGameOptionResponse
import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.service.BalanceGameService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Balance Game",
    description = "밸런스 게임 API"
)
@RestController
@RequestMapping("/v1/balance-game")
@Validated
class BalanceGameController(
    private val balanceGameService: BalanceGameService,
) {

    @Operation(
        summary = "오늘의 밸런스 게임 조회",
        description = "밸런스 게임 정보와 커플의 선택 정보를 조회합니다.",
    )
    @GetMapping("/today")
    fun getTodayBalanceGame(): CaramelApiResponse<GetBalanceGameResponse> {
        val response = balanceGameService.getTodayBalanceGameInfo()
        return response.succeed()
    }

    @Operation(
        summary = "밸런스 게임 선택",
        description = "밸런스 게임을 선택합니다."
    )
    @PostMapping("/{gameId}")
    fun chooseBalanceGameOption(
        @PathVariable @Positive(message = "The game ID must be positive.") gameId: Long,
        @RequestBody @Valid request: ChooseBalanceGameOptionRequest,
    ): CaramelApiResponse<ChooseBalanceGameOptionResponse> {
        val response = balanceGameService.chooseBalanceGameOption(gameId, request)
        return response.succeed()
    }
}