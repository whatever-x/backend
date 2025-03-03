package com.whatever.domain.calendarevent.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "휴일 상세 정보 리스트 응답 DTO")
data class HolidayDetailListResponse(
    @Schema(description = "휴일 상세 정보 리스트")
    val holidayList: List<HolidayDetailDto>
)

@Schema(description = "휴일 상세 정보 DTO")
data class HolidayDetailDto(
    @Schema(description = "휴일 id", example = "1")
    val id: Long,

    @Schema(description = "휴일 타입 (예: SOLAR_TERM, HOLIDAY)", example = "SOLAR_TERM")
    val type: String,

    @Schema(description = "휴일 날짜 (yyyy-MM-dd)", example = "2025-12-22")
    val date: LocalDate,

    @Schema(description = "휴일 이름")
    val name: String
)