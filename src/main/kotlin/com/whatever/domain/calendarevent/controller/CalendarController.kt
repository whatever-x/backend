package com.whatever.domain.calendarevent.controller

import com.whatever.domain.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.domain.calendarevent.controller.dto.response.CalendarDetailResponse
import com.whatever.domain.calendarevent.controller.dto.response.CalendarEventsDto
import com.whatever.domain.calendarevent.controller.dto.response.HolidayDetailListResponse
import com.whatever.domain.calendarevent.scheduleevent.service.ScheduleEventService
import com.whatever.domain.calendarevent.specialday.service.SpecialDayService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.YearMonth

@Tag(
    name = "Calendar",
    description = "캘린더 API"
)
@RestController
@RequestMapping("/v1/calendar")
class CalendarController(
    private val scheduleEventService: ScheduleEventService,
    private val specialDayService: SpecialDayService
) {

    @Operation(
        summary = "캘린더 조회",
        description = "캘린더 이벤트들을 조회합니다.",
    )
    @GetMapping
    fun getCalendar(@ParameterObject queryParameter: GetCalendarQueryParameter): CaramelApiResponse<CalendarDetailResponse> {
        val schedules = scheduleEventService.getSchedule(
            startDate = queryParameter.startDate,
            endDate = queryParameter.endDate,
            userTimeZone = queryParameter.userTimeZone
        )

        val calendarResult = CalendarEventsDto(scheduleList = schedules)
        return CalendarDetailResponse(calendarResult = calendarResult).succeed()
    }

    @Operation(
        summary = "휴일 조회",
        description = "캘린더에 기본으로 표시되어야하는 특별한 날 중 휴일을 반환합니다.",
    )
    @GetMapping("/holidays")
    fun getHolidays(
        @RequestParam("yearMonth") yearMonth: YearMonth,
    ): CaramelApiResponse<HolidayDetailListResponse> {
        val response = specialDayService.getHolidays(yearMonth)
        return response.succeed()
    }
}