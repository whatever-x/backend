package com.whatever.caramel.api.balancegame.controller

import com.whatever.caramel.api.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.caramel.api.balancegame.controller.dto.response.ChooseBalanceGameOptionResponse
import com.whatever.caramel.api.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.caramel.common.response.CaramelApiResponse
import com.whatever.caramel.common.response.succeed
import com.whatever.caramel.domain.balancegame.service.BalanceGameService
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    name = "밸런스 게임 API",
    description = "밸런스 게임 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/balance-game")
@Validated
class BalanceGameController(
    private val balanceGameService: BalanceGameService,
) {

    @Operation(
        summary = "오늘의 밸런스 게임 조회",
        description = """
            ### Asia/Seoul 시간 기준 오늘의 밸런스 게임을 조회합니다.
            
            - 커플 멤버들이 선택지를 골랐다면, 해당 정보를 포함하여 전송합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "밸런스 게임 정보 + 커플 멤버 선택 정보"),
        ]
    )
    @GetMapping("/today")
    fun getTodayBalanceGame(): CaramelApiResponse<GetBalanceGameResponse> {
        val balanceGameVo = balanceGameService.getTodayBalanceGameInfo()

        val userChoiceOptionVos = balanceGameService.getCoupleMemberChoices(
            coupleId = getCurrentUserCoupleId(),
            gameId = balanceGameVo.id,
        )

        return GetBalanceGameResponse.from(
            gameVo = balanceGameVo,
            myChoice = userChoiceOptionVos.firstOrNull { it.userId == getCurrentUserId() },
            partnerChoice = userChoiceOptionVos.firstOrNull { it.userId != getCurrentUserId() },
        ).succeed()
    }

    @Operation(
        summary = "밸런스 게임 선택",
        description = """
            ### 밸런스 게임의 선택지를 고릅니다.
            
            - 상대방이 이미 선택지를 골랐다면, 상대방의 선택 정보를 포함하여 전송합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "밸런스 게임 정보 + 커플 멤버 선택 정보"),
        ]
    )
    @PostMapping("/{gameId}")
    fun chooseBalanceGameOption(
        @PathVariable @Positive(message = "The game ID must be positive.") gameId: Long,
        @RequestBody @Valid request: ChooseBalanceGameOptionRequest,
    ): CaramelApiResponse<ChooseBalanceGameOptionResponse> {
        val coupleChoiceOptionVo = balanceGameService.chooseBalanceGameOption(
            gameId = gameId,
            selectedOptionId = request.optionId,
            coupleId = getCurrentUserCoupleId(),
            requestUserId = getCurrentUserId(),
        )
        val balanceGameVo = balanceGameService.getTodayBalanceGameInfo()
        return ChooseBalanceGameOptionResponse.from(
            gameVo = balanceGameVo,
            myChoice = coupleChoiceOptionVo.myChoice,
            partnerChoice = coupleChoiceOptionVo.partnerChoice,
        ).succeed()
    }
}
