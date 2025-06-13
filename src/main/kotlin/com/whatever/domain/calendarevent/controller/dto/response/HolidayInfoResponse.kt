package com.whatever.domain.calendarevent.controller.dto.response

import com.whatever.domain.calendarevent.specialday.model.SpecialDay
import com.whatever.domain.calendarevent.specialday.model.SpecialDayType
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

    @Schema(description = "휴일 타입 (예: HOLI)", example = "HOLI")
    val type: SpecialDayType,

    @Schema(description = "휴일 날짜 (yyyy-MM-dd)", example = "2025-12-22")
    val date: LocalDate,

    @Schema(description = "휴일 이름")
    val name: String,

    @Schema(description = "공휴일 여부")
    val isHoliday: Boolean,
) {
    companion object {
        fun from(holiday: SpecialDay): HolidayDetailDto? {
            if (holiday.type != SpecialDayType.HOLI) {
                return null
            }
            return HolidayDetailDto(
                id = holiday.id,
                type = holiday.type,
                date = holiday.locDate,
                name = holiday.dateName,
                isHoliday = holiday.isHoliday,
            )
        }
    }
}