package com.whatever.caramel.api.calendarevent.scheduleevent.controller

import com.whatever.caramel.api.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.caramel.api.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.caramel.api.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.caramel.api.calendarevent.scheduleevent.controller.dto.GetScheduleResponse
import com.whatever.caramel.api.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.caramel.api.content.controller.dto.response.ContentSummaryResponse
import com.whatever.caramel.common.response.CaramelApiResponse
import com.whatever.caramel.common.response.succeed
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserId
import com.whatever.caramel.domain.calendarevent.service.ScheduleEventService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "일정 API",
    description = "일정 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/calendar/schedules")
class ScheduleController(
    private val scheduleEventService: ScheduleEventService,
) {

    @Operation(
        summary = "일정 기간별 조회",
        description = """### 주어진 기간에 포함된 일정을 조회합니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "일정 정보 리스트"),
        ]
    )
    @GetMapping
    fun getSchedules(
        @ParameterObject queryParameter: GetCalendarQueryParameter,
    ): CaramelApiResponse<List<ScheduleDetailDto>> {
        val scheduleDetailsVo = scheduleEventService.getSchedules(
            startDate = queryParameter.startDate,
            endDate = queryParameter.endDate,
            userTimeZone = queryParameter.userTimeZone,
            currentUserCoupleId = getCurrentUserCoupleId(),
        )

        return scheduleDetailsVo.scheduleDetailVoList
            .map { ScheduleDetailDto.from(it) }
            .succeed()
    }

    @Operation(
        summary = "일정 단건 조회",
        description = """
            ### schedule-id에 해당하는 일정을 조회합니다.
            
            - 같은 커플에 속한 유저의 일정만 조회할 수 있습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "일정 정보 + 연관 태그 리스트"),
        ]
    )
    @GetMapping("/{schedule-id}")
    fun getSchedule(
        @PathVariable("schedule-id") scheduleId: Long,
    ): CaramelApiResponse<GetScheduleResponse> {
        val getScheduleVo = scheduleEventService.getSchedule(
            scheduleId = scheduleId,
            ownerCoupleId = getCurrentUserCoupleId(),
        )
        return GetScheduleResponse.from(getScheduleVo).succeed()
    }

    @Operation(
        summary = "일정 생성",
        description = """
            ### 일정을 생성합니다.
            
            - `title`과 `description` 둘 중 하나는 입력값이 있어야합니다.
            
            - `title`은 Blank일 수 없습니다.
            
            - `description`은 Blank일 수 없습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "생성 정보 요약"),
        ]
    )
    @PostMapping
    fun createSchedule(
        @Valid @RequestBody request: CreateScheduleRequest,
    ): CaramelApiResponse<ContentSummaryResponse> {
        val contentSummaryVo = scheduleEventService.createSchedule(
            scheduleVo = request.toVo(),
            currentUserId = getCurrentUserId(),
            currentUserCoupleId = getCurrentUserCoupleId(),
        )
        return ContentSummaryResponse.from(contentSummaryVo).succeed()
    }

    @Operation(
        summary = "일정 수정",
        description = """
            ### 일정을 수정합니다.
            
            1. 일정의 정보를 단순히 수정하고 싶을 경우
                - `startDateTime`을 포함해야 합니다.
            
            2. 일정을 메모로 전환하고 싶은 경우
                - 날짜와 타임존 정보를 포함하지 않거나, null로 설정하여 요청을 전송합니다.
            
            - title과 description의 제약 조건은 `일정 생성`과 동일합니다.
        """,
    )
    @PutMapping("/{schedule-id}")
    fun updateSchedule(
        @PathVariable("schedule-id") scheduleId: Long,
        @Valid @RequestBody request: UpdateScheduleRequest,
    ): CaramelApiResponse<Unit> {
        scheduleEventService.updateSchedule(
            scheduleId = scheduleId,
            currentUserId = getCurrentUserId(),
            currentUserCoupleId = getCurrentUserCoupleId(),
            scheduleVo = request.toVo(),
        )
        return CaramelApiResponse.succeed()
    }

    @Operation(
        summary = "일정 삭제",
        description = """
            ### schedule-id에 해당하는 일정을 삭제합니다.
            
            - 같은 커플에 속한 유저의 일정만 삭제할 수 있습니다. 
        """,
    )
    @DeleteMapping("/{schedule-id}")
    fun deleteSchedule(@PathVariable("schedule-id") scheduleId: Long): CaramelApiResponse<Unit> {
        scheduleEventService.deleteSchedule(
            scheduleId = scheduleId,
            currentUserId = getCurrentUserId(),
            currentUserCoupleId = getCurrentUserCoupleId(),
        )
        return CaramelApiResponse.succeed()
    }
}
