package com.whatever.domain.content.controller.dto.request

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "콘텐츠 생성 요청 모델")
data class CreateContentRequest(
    @Schema(description = "콘텐츠 제목", example = "맛집 리스트")
    val title: String,

    @Schema(description = "콘텐츠 설명", example = "어제 함께 갔던 맛집들")
    val description: String,

    @Schema(
        description = "희망일",
        example = "2025-02-16",
        requiredMode = NOT_REQUIRED,
    )
    val wishDate: LocalDate? = null,

    @Schema(
        description = "완료 여부",
        requiredMode = NOT_REQUIRED,
    )
    val isCompleted: Boolean = false,

    @Schema(
        description = "태그 번호 리스트",
        requiredMode = NOT_REQUIRED,
    )
    val tagList: List<TagIdDto> = emptyList(),

    @Schema(
        description = "일정 정보 (캘린더 추가시에만 사용)",
        requiredMode = NOT_REQUIRED
    )
    val dateTimeInfo: DateTimeInfoDto? = null
)

@Schema(description = "태그 정보 모델")
data class TagIdDto(
    @Schema(description = "태그 id", example = "1")
    val tagId: Long,
)

@Schema(description = "일정 정보 모델")
data class DateTimeInfoDto(
    @Schema(description = "시작일", example = "2025-02-16T18:26:40")
    val startDateTime: LocalDateTime,

    @Schema(description = "시작일 타임존", example = "Asia/Seoul")
    val startTimezone: String,

    @Schema(
        description = "종료일. 만약 null이면 시작일의 자정으로 대체됩니다.",
        example = "2025-02-16T23:59:59",
        requiredMode = NOT_REQUIRED,
    )
    val endDateTime: LocalDateTime? = null,

    @Schema(
        description = "종료일 타임존. 만약 null이면 시작 타임존 값으로 대체됩니다.",
        example = "Asia/Seoul",
        requiredMode = NOT_REQUIRED,
    )
    val endTimezone: String? = null
)
