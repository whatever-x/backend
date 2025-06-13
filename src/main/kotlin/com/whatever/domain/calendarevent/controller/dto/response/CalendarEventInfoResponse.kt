package com.whatever.domain.calendarevent.controller.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class CalendarDetailResponse(
    val calendarResult: CalendarEventsDto,
)

data class CalendarEventsDto(
    val scheduleList: List<ScheduleDetailDto>,
)

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
    val description: String?
)

data class ScheduleDetailDtoV2(  // 반복 도입후 해당 response dto로 교체
    val scheduleId: Long,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val startDateTimezone: String,
    val endDateTimezone: String,
    val recurrenceRule: RecurrenceRuleDto,
    val recurrenceDateTimeList: List<RecurrenceDateTimeDto>,
    val isCompleted: Boolean,
    val parentScheduleId: Long?,  // 예외 처리된 반복일 경우, 부모(원본) 일정 id
    val title: String,
    val description: String?
)

data class RecurrenceRuleDto(
    // TODO(준용): 규칙 상세 추가 예정
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val recurrenceDesc: String
)

data class RecurrenceDateTimeDto(
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
)