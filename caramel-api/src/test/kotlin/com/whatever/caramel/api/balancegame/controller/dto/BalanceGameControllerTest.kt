package com.whatever.caramel.api.balancegame.controller.dto

import com.whatever.caramel.api.ControllerTestSupport
import com.whatever.domain.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import org.junit.jupiter.api.DisplayName
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.Test

class BalanceGameControllerTest : ControllerTestSupport() {

    @DisplayName("")
    @Test
    fun getTodayBalanceGame() {
        // when, then
        mockMvc.get("/v1/balance-game/today")
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("")
    @Test
    fun chooseBalanceGameOption() {
        // given
        val gameId = 1L
        val request = ChooseBalanceGameOptionRequest(
            optionId = 1L,
        )

        // when, then
        mockMvc.post("/v1/balance-game/${gameId}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("")
    @Test
    fun chooseBalanceGameOption_WithIllegalGameId() {
        // given
        val gameId = -1L
        val request = ChooseBalanceGameOptionRequest(
            optionId = 1L,
        )

        // when, then
        mockMvc.post("/v1/balance-game/${gameId}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @DisplayName("")
    @Test
    fun chooseBalanceGameOption_WithIllegalOptionId() {
        // given
        val gameId = 1L
        val request = ChooseBalanceGameOptionRequest(
            optionId = -1L,
        )

        // when, then
        mockMvc.post("/v1/balance-game/${gameId}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
            }
    }
}
