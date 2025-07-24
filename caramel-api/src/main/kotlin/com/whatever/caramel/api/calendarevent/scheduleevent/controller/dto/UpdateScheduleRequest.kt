package com.whatever.caramel.api.calendarevent.scheduleevent.controller.dto

import com.whatever.caramel.domain.calendarevent.vo.UpdateScheduleVo
import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_DESCRIPTION_LENGTH
import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import com.whatever.caramel.domain.content.vo.ContentOwnerType
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "일정 업데이트 요청 모델")
data class UpdateScheduleRequest(

    @Schema(description = "선택한 일자", example = "2025-02-16")
    val selectedDate: LocalDate,

    @Schema(description = "콘텐츠 제목. title, description 둘 중 하나는 필수입니다.", example = "맛집 리스트", nullable = true)
    @field:CodePointLength(max = MAX_TITLE_LENGTH, message = "제목은 최대 ${MAX_TITLE_LENGTH}자까지 가능합니다.")
    val title: String? = null,

    @Schema(description = "콘텐츠 설명. title, description 둘 중 하나는 필수입니다.", example = "어제 함께 갔던 맛집들", nullable = true)
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH, message = "설명은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.")
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

    @Schema(description = "소유자 타입 (ME: 나, PARTNER: 상대방, US: 우리)")
    val ownerType: ContentOwnerType,
) {
    fun toVo(): UpdateScheduleVo {
        return UpdateScheduleVo(
            selectedDate = this.selectedDate,
            title = this.title,
            description = this.description,
            isCompleted = this.isCompleted,
            startDateTime = this.startDateTime,
            startTimeZone = this.startTimeZone,
            endDateTime = this.endDateTime,
            endTimeZone = this.endTimeZone,
            tagIds = this.tagIds,
            ownerType = this.ownerType,
        )
    }
}
