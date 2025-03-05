package com.whatever.domain.calendarevent.scheduleevent.controller

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.global.exception.dto.CaramelApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Calendar-Schedule",
    description = "캘린더의 요소 중 일정에 관련된 API"
)
@RestController
@RequestMapping("/v1/calendar/schedules")
class ScheduleController {

    @Operation(
        summary = "더미 일정 수정",
        description = "일정을 수정합니다.",
    )
    @PutMapping("/{schedule-id}")
    fun updateSchedule(
        @PathVariable("schedule-id") scheduleId: Long,
        @RequestBody request: UpdateScheduleRequest,
    ): CaramelApiResponse<Unit> {

        // TODO(준용): 구현 필요
        return CaramelApiResponse.succeed()
    }

    @Operation(
        summary = "더미 일정 삭제",
        description = "일정을 삭제합니다.",
    )
    @DeleteMapping("/{schedule-id}")
    fun deleteSchedule(@PathVariable("schedule-id") scheduleId: Long): CaramelApiResponse<Unit> {

        // TODO(준용): 구현 필요
        return CaramelApiResponse.succeed()
    }
}