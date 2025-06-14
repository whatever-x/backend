package com.whatever.domain.calendarevent.scheduleevent.controller

import com.whatever.domain.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.domain.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.GetScheduleResponse
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.service.ScheduleEventService
import com.whatever.domain.content.controller.dto.response.ContentSummaryResponse
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
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
        summary = "일정 기간별 조회",
        description = "주어진 기간 내의 일정을 조회합니다.",
    )
    @GetMapping
    fun getSchedules(
        @ParameterObject queryParameter: GetCalendarQueryParameter
    ): CaramelApiResponse<List<ScheduleDetailDto>> {
        val schedulesResponse = scheduleEventService.getSchedules(
            startDate = queryParameter.startDate,
            endDate = queryParameter.endDate,
            userTimeZone = queryParameter.userTimeZone
        )

        return schedulesResponse.succeed()
    }

    @Operation(
        summary = "일정 조회",
        description = "id에 해당하는 일정을 조회합니다. 커플 멤버가 작성한 일정만 조회 가능합니다.",
    )
    @GetMapping("/{schedule-id}")
    fun getSchedule(
        @PathVariable("schedule-id") scheduleId: Long
    ): CaramelApiResponse<GetScheduleResponse> {
        val scheduleResponse = scheduleEventService.getSchedule(scheduleId = scheduleId)
        return scheduleResponse.succeed()
    }

    @Operation(
        summary = "일정 생성",
        description = "새로운 일정을 생성합니다.",
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
        description = "일정을 수정합니다. 일정을 메모로 변경할 때 사용할 수 있습니다.",
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