package com.whatever.domain.content.controller.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import java.time.LocalDate

@Schema(description = "콘텐츠 수정 요청 모델")
data class UpdateContentRequest(
    @Schema(description = "콘텐츠 제목", example = "맛집 리스트")
    val title: String?,

    @Schema(description = "콘텐츠 설명", example = "어제 함께 갔던 맛집들")
    val description: String?,

//    @Schema(
//        description = "희망일",
//        example = "2025-02-16",
//        requiredMode = NOT_REQUIRED,
//    )
//    val wishDate: LocalDate? = null,

    @Schema(description = "완료 여부", requiredMode = NOT_REQUIRED)
    val isCompleted: Boolean = false,

    @Schema(description = "태그 번호 리스트", requiredMode = NOT_REQUIRED)
    val tagList: List<TagIdDto> = emptyList(),

    @Schema(description = "일정 정보 (캘린더 추가시에만 사용)", requiredMode = NOT_REQUIRED)
    val dateTimeInfo: DateTimeInfoDto? = null
)