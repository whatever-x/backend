package com.whatever.caramel.api.calendarevent.controller.dto.response

import com.whatever.caramel.domain.calendarevent.vo.ScheduleDetailVo
import com.whatever.caramel.domain.content.vo.ContentOwnerType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class CalendarDetailResponse(
    val calendarResult: CalendarEventsDto,
)

data class CalendarEventsDto(
    val scheduleList: List<ScheduleDetailDto>,
)

@Schema(description = "일정 상세 정보 DTO")
data class ScheduleDetailDto(
    @Schema(description = "현재 스케줄 id", example = "2")
    val scheduleId: Long,

    @Schema(description = "스케줄 시작일", example = "2025-03-12 00:00:00")
    val startDateTime: LocalDateTime,

    @Schema(description = "스케줄 종료일", example = "2025-03-12 23:59:59")
    val endDateTime: LocalDateTime,

    @Schema(description = "시작 타임존", example = "Asia/Seoul")
    val startDateTimezone: String,

    @Schema(description = "종료 타임존", example = "Asia/Seoul")
    val endDateTimezone: String,

    @Schema(description = "일정 완료 여부 (현시점 필요 없음)", example = "false")
    val isCompleted: Boolean,

    @Schema(description = "부모(원본) 일정 id. 예외 처리된 반복일 경우 사용", example = "null")
    val parentScheduleId: Long? = null,

    @Schema(description = "캘린더에 표시되는 제목", example = "캘린더에 표시되는 제목")
    val title: String?,

    @Schema(description = "캘린더 본문", example = "본문입니다.")
    val description: String?,

    @Schema(description = "소유자 타입")
    val ownerType: ContentOwnerType,
) {
    companion object {
        fun from(scheduleDetailVo: ScheduleDetailVo): ScheduleDetailDto {
            return ScheduleDetailDto(
                scheduleId = scheduleDetailVo.scheduleId,
                startDateTime = scheduleDetailVo.startDateTime,
                endDateTime = scheduleDetailVo.endDateTime,
                startDateTimezone = scheduleDetailVo.startDateTimezone,
                endDateTimezone = scheduleDetailVo.endDateTimezone,
                isCompleted = scheduleDetailVo.isCompleted,
                parentScheduleId = scheduleDetailVo.parentScheduleId,
                title = scheduleDetailVo.title,
                description = scheduleDetailVo.description,
                ownerType = scheduleDetailVo.ownerType,
            )
        }
    }
}
