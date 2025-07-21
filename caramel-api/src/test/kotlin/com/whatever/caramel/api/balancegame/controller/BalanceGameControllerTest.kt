package com.whatever.caramel.api.balancegame.controller

import com.whatever.SecurityUtil
import com.whatever.caramel.api.ControllerTestSupport
import com.whatever.caramel.api.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.domain.balancegame.vo.BalanceGameOptionVo
import com.whatever.domain.balancegame.vo.BalanceGameVo
import com.whatever.domain.balancegame.vo.CoupleChoiceOptionVo
import com.whatever.domain.balancegame.vo.UserChoiceOptionVo
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.Test

class BalanceGameControllerTest : ControllerTestSupport() {

    @BeforeEach
    fun setUp() {
        mockkStatic(SecurityUtil::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SecurityUtil::class)
    }

    @DisplayName("")
    @Test
    fun getTodayBalanceGame() {
        whenever(balanceGameService.getTodayBalanceGameInfo())
            .thenReturn(BalanceGameVo(
                id = 0L,
                gameDate = DateTimeUtil.localNow().toLocalDate(),
                question = "test-question",
                options = listOf(BalanceGameOptionVo(0L, "test-option"))
            ))
        whenever(balanceGameService.getCoupleMemberChoices(any(), any()))
            .thenReturn(listOf(UserChoiceOptionVo(0L, 0L, 0L, 0L)))
        every { SecurityUtil.getCurrentUserCoupleId() } returns 0L
        every { SecurityUtil.getCurrentUserId() } returns 0L

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
        whenever(balanceGameService.getTodayBalanceGameInfo())
            .thenReturn(BalanceGameVo(
                id = gameId,
                gameDate = DateTimeUtil.localNow().toLocalDate(),
                question = "test-question",
                options = listOf(BalanceGameOptionVo(0L, "test-option"))
            ))
        whenever(balanceGameService.chooseBalanceGameOption(
            gameId = any(),
            selectedOptionId = any(),
            coupleId = any(),
            requestUserId = any(),
        )).thenReturn(CoupleChoiceOptionVo(null, null))
        every { SecurityUtil.getCurrentUserCoupleId() } returns 0L
        every { SecurityUtil.getCurrentUserId() } returns 0L

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
