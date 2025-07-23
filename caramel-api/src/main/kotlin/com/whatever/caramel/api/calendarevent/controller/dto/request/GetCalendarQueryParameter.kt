package com.whatever.caramel.api.calendarevent.controller.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.ZoneId

@Schema(description = "캘린더 이벤트 조회 요청 객체")
data class GetCalendarQueryParameter(
    @field:Parameter(
        description = "조회 시작일(월의 초일 권장)",
        `in` = ParameterIn.QUERY,
        required = true,
        example = "2025-03-01",
    )
    val startDate: LocalDate,

    @field:Parameter(
        description = "조회 종료일(월의 말일 권장)",
        `in` = ParameterIn.QUERY,
        required = true,
        example = "2025-03-31",
    )
    val endDate: LocalDate,

    @field:Parameter(
        description = "유저의 현재 타임존",
        `in` = ParameterIn.QUERY,
        example = "Asia/Seoul",
    )
    val userTimeZone: String = ZoneId.of("Asia/Seoul").id,
)
