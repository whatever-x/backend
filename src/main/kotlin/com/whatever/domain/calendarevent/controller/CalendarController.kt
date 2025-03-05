package com.whatever.domain.calendarevent.controller

import com.whatever.domain.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.domain.calendarevent.controller.dto.response.*
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime

@Tag(
    name = "Calendar",
    description = "캘린더 API"
)
@RestController
@RequestMapping("/v1/calendar")
class CalendarController {

    @Operation(
        summary = "더미 캘린더 조회",
        description = "캘린더 이벤트들을 조회합니다.",
    )
    @GetMapping
    fun getCalendar(@ParameterObject queryParameter: GetCalendarQueryParameter): CaramelApiResponse<CalendarDetailResponse> {

        // TODO(준용): 구현 필요
        val scheduleList = mutableListOf<ScheduleDetailDto>()
        var currentDate = queryParameter.startDate
        var idCnt = 1L
        while (currentDate <= queryParameter.endDate) {
            scheduleList.add(
                ScheduleDetailDto(
                    scheduleId = idCnt++,
                    startDateTime = currentDate.atStartOfDay(),
                    endDateTime = currentDate.atTime(LocalTime.MAX),
                    startDateTimezone = queryParameter.userTimeZone,
                    endDateTimezone = queryParameter.userTimeZone,
                    isCompleted = false,
                    parentScheduleId = null,
                    title = "캘린더에 표시되는 제목 - $currentDate",
                    description = "본문입니다."
                )
            )
            currentDate = currentDate.plusDays(2)
        }

        val calendarResult = CalendarEventsDto(scheduleList = scheduleList)
        return CalendarDetailResponse(calendarResult = calendarResult).succeed()
    }

    @Operation(
        summary = "더미 휴일 조회",
        description = "캘린더에 기본으로 표시되어야하는 특별한 날을 반환합니다.",
    )
    @GetMapping("/holidays")
    fun getHolidays(): CaramelApiResponse<HolidayDetailListResponse> {

        // TODO(준용): 구현 필요
        val contentList = listOf(
            HolidayDetailDto(
                id = 1,
                type = "SOLAR_TERM",
                date = LocalDate.parse("2025-12-22"),
                name = "동지"
            ),
            HolidayDetailDto(
                id = 4,
                type = "HOLIDAY",
                date = LocalDate.parse("2025-12-25"),
                name = "성탄절"
            )
        )
        return HolidayDetailListResponse(contentList).succeed()
    }
}