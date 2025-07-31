package com.whatever.caramel.api.balancegame.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

@Schema(description = "밸런스게임 선택지 선택 모델")
data class ChooseBalanceGameOptionRequest(
    @Schema(description = "밸런스게임 option id", example = "1")
    @field:Positive(message = "The option ID must be positive.")
    val optionId: Long,
)
