package com.whatever.domain.calendarevent.scheduleevent.controller

import com.whatever.domain.ControllerTestSupport
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.global.exception.GlobalExceptionCode
import com.whatever.util.DateTimeUtil
import org.junit.jupiter.api.DisplayName
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.put
import kotlin.test.Test

class ScheduleControllerTest : ControllerTestSupport() {

    companion object {
        val SCHEDULE_ID = 1L
    }

    @DisplayName("스케줄을 업데이트 한다.")
    @Test
    fun updateSchedule() {
        // given
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "new title",
            description = "new description",
            isCompleted = false,
            startDateTime = DateTimeUtil.localNow(),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        mockMvc.put("/v1/calendar/schedules/${SCHEDULE_ID}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isOk() }
            }
    }

    @DisplayName("스케줄을 업데이트 시 제목은 공백을 제외한 문자여야 한다.")
    @Test
    fun updateSchedule_WithBlankTitle() {
        // given
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "     ",  // Blank Title
            description = "new description",
            isCompleted = false,
            startDateTime = DateTimeUtil.localNow(),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        mockMvc.put("/v1/calendar/schedules/${SCHEDULE_ID}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

    @DisplayName("스케줄을 업데이트 시 본문은 공백을 제외한 문자여야 한다.")
    @Test
    fun updateSchedule_WithBlankDescription() {
        // given
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "new title",
            description = "     ",  // Blank Title
            isCompleted = false,
            startDateTime = DateTimeUtil.localNow(),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        mockMvc.put("/v1/calendar/schedules/${SCHEDULE_ID}") {
            content = objectMapper.writeValueAsString(request)
            contentType = MediaType.APPLICATION_JSON
        }
            .andDo { print() }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value(GlobalExceptionCode.ARGS_VALIDATION_FAILED.code) }
            }
    }

}