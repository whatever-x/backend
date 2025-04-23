package com.whatever.domain.calendarevent.scheduleevent.controller.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "콘텐츠 생성 요청 모델")
data class UpdateScheduleRequest(

    @Schema(description = "선택한 일자", example = "2025-02-16")
    val selectedDate: LocalDate,

    @Schema(description = "콘텐츠 제목. title, description 둘 중 하나는 필수입니다.", example = "맛집 리스트", nullable = true)
    @field:NotBlank(message = "제목은 공백일 수 없습니다.")
    val title: String? = null,

    @field:NotBlank(message = "본문은 공백일 수 없습니다.")
    @Schema(description = "콘텐츠 설명. title, description 둘 중 하나는 필수입니다.", example = "어제 함께 갔던 맛집들", nullable = true)
    val description: String? = null,

    @Schema(description = "완료 여부")
    val isCompleted: Boolean,

    @Schema(description = "시작일", example = "2025-02-16T18:26:40", nullable = true)
    val startDateTime: LocalDateTime? = null,

    @Schema(description = "시작일 타임존", example = "Asia/Seoul", nullable = true)
    val startTimeZone: String? = null,

    @Schema(description = "종료일", example = "2025-02-16T23:59:59", nullable = true)
    val endDateTime: LocalDateTime? = null,

    @Schema(description = "종료일 타임존", example = "Asia/Seoul", nullable = true)
    val endTimeZone: String? = null,

    @Schema(description = "태그 번호 리스트")
    val tagIds: Set<Long> = emptySet(),
)
