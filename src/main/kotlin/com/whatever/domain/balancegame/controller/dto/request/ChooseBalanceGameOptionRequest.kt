package com.whatever.domain.balancegame.controller.dto.request

import jakarta.validation.constraints.Positive

data class ChooseBalanceGameOptionRequest(
    @field:Positive(message = "The option ID must be positive.")
    val optionId: Long,
)