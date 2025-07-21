package com.whatever.caramel.api.calendarevent.controller

import com.whatever.caramel.api.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.caramel.api.calendarevent.controller.dto.response.CalendarDetailResponse
import com.whatever.caramel.api.calendarevent.controller.dto.response.CalendarEventsDto
import com.whatever.caramel.api.calendarevent.controller.dto.response.HolidayDetailDto
import com.whatever.caramel.api.calendarevent.controller.dto.response.HolidayDetailListResponse
import com.whatever.caramel.api.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.caramel.common.response.CaramelApiResponse
import com.whatever.caramel.common.response.succeed
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.domain.calendarevent.service.ScheduleEventService
import com.whatever.domain.specialday.service.SpecialDayService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Year
import java.time.YearMonth

@Tag(
    name = "캘린더 API",
    description = "캘린더에 표시될 일정, 기념일, 공휴일 등 다양한 이벤트를 조회하는 API"
)
@RestController
@RequestMapping("/v1/calendar")
class CalendarController(
    private val scheduleEventService: ScheduleEventService,
    private val specialDayService: SpecialDayService,
) {

    @Operation(
        summary = "캘린더 조회",
        description = """
            ### 캘린더에 표시되어야 하는 항목들을 조회합니다.
            
            - 추후 캘린더에 추가될 다른 이벤트가 생길경우 해당 api를 통해 한번에 조회할 수 있습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "캘린더 이벤트 리스트"),
        ]
    )
    @GetMapping
    fun getCalendar(@ParameterObject queryParameter: GetCalendarQueryParameter): CaramelApiResponse<CalendarDetailResponse> {
        val scheduleDetailsVo = scheduleEventService.getSchedules(
            startDate = queryParameter.startDate,
            endDate = queryParameter.endDate,
            userTimeZone = queryParameter.userTimeZone,
            currentUserCoupleId = getCurrentUserCoupleId(),
        )

        val calendarResult = CalendarEventsDto(
            scheduleList = scheduleDetailsVo.scheduleDetailVoList
                .map { ScheduleDetailDto.from(it) }
        )
        return CalendarDetailResponse(calendarResult = calendarResult).succeed()
    }

    @Operation(
        summary = "휴일 조회",
        description = """### 조회 범위에 속한 휴일들을 조회합니다.""",
    )
    @GetMapping("/holidays")
    fun getHolidays(
        @RequestParam("startYearMonth") startYearMonth: YearMonth,
        @RequestParam("endYearMonth") endYearMonth: YearMonth,
    ): CaramelApiResponse<HolidayDetailListResponse> {
        val holidayDetailListVo = specialDayService.getHolidays(
            startYearMonth = startYearMonth,
            endYearMonth = endYearMonth,
        )
        return HolidayDetailListResponse(
            holidayList = holidayDetailListVo.holidayList.map { HolidayDetailDto.from(it) }
        ).succeed()
    }

    @Operation(
        summary = "휴일 연도 조회",
        description = """### 요청한 연도에 해당하는 휴일들을 조회합니다.""",
    )
    @GetMapping("/holidays/year")
    fun getHolidaysInYear(
        @RequestParam("year") year: Year,
    ): CaramelApiResponse<HolidayDetailListResponse> {
        val holidayDetailListVo = specialDayService.getHolidaysInYear(year)
        return HolidayDetailListResponse(
            holidayList = holidayDetailListVo.holidayList.map { HolidayDetailDto.from(it) }
        ).succeed()
    }
}
