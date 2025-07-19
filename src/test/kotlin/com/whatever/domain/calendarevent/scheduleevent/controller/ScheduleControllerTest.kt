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

    @DisplayName("스케줄을 업데이트 시 제목은 최대 30글자여야 한다.")
    @Test
    fun updateSchedule_WithTitleLength30() {
        // given
        val titleLength30 = "\uD83D\uDC4D".repeat(30)  // 👍
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = titleLength30,
            description = "test-desc",
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

    @DisplayName("스케줄을 업데이트 시 제목이 30글자 초과라면 예외가 발생한다.")
    @Test
    fun updateSchedule_WithTitleLength31() {
        // given
        val titleLength31 = "\uD83D\uDC4D".repeat(31)  // 👍
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = titleLength31,
            description = "test-desc",
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

    @DisplayName("스케줄을 업데이트 시 본문은 최대 5000글자여야 한다.")
    @Test
    fun updateSchedule_WithDescriptionLength5000() {
        // given
        val descriptionLength5000 = "\uD83D\uDC4D".repeat(5000)
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "test-title",
            description = descriptionLength5000,
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

    @DisplayName("스케줄을 업데이트 시 제목이 30글자 초과라면 예외가 발생한다.")
    @Test
    fun updateSchedule_WithDescriptionLength5001() {
        // given
        val descriptionLength5001 = "\uD83D\uDC4D".repeat(5001)
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "test-title",
            description = descriptionLength5001,
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
