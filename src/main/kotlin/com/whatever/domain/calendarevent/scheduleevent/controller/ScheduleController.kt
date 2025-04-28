package com.whatever.domain.calendarevent.scheduleevent.controller

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.service.ScheduleEventService
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Calendar-Schedule",
    description = "캘린더의 요소 중 일정에 관련된 API"
)
@RestController
@RequestMapping("/v1/calendar/schedules")
class ScheduleController(
    private val scheduleEventService: ScheduleEventService
) {

    @Operation(
        summary = "일정 생성",
        description = "일정을 생성합니다.",
    )
    @PostMapping
    fun createSchedule(
        @Valid @RequestBody request: CreateScheduleRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        val response = scheduleEventService.createSchedule(
            request = request,
        )
        return response.succeed()
    }

    @Operation(
        summary = "일정 수정",
        description = "일정을 수정합니다.",
    )
    @PutMapping("/{schedule-id}")
    fun updateSchedule(
        @PathVariable("schedule-id") scheduleId: Long,
        @Valid @RequestBody request: UpdateScheduleRequest,
    ): CaramelApiResponse<Unit> {
        scheduleEventService.updateSchedule(
            scheduleId = scheduleId,
            request = request,
        )
        return CaramelApiResponse.succeed()
    }

    @Operation(
        summary = "일정 삭제",
        description = "일정을 삭제합니다.",
    )
    @DeleteMapping("/{schedule-id}")
    fun deleteSchedule(@PathVariable("schedule-id") scheduleId: Long): CaramelApiResponse<Unit> {
        scheduleEventService.deleteSchedule(scheduleId)
        return CaramelApiResponse.succeed()
    }
}