package com.whatever.caramel.api.content.controller.dto.request

import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo
import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_DESCRIPTION_LENGTH
import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import com.whatever.caramel.domain.content.vo.ContentOwnerType
import com.whatever.caramel.domain.content.vo.CreateContentRequestVo
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDateTime

@Schema(description = "콘텐츠 생성 요청 모델")
data class CreateContentRequest(
    @Schema(description = "콘텐츠 제목. 제목은 최대 ${MAX_TITLE_LENGTH}자까지 가능합니다.", example = "맛집 리스트")
    @field:CodePointLength(max = MAX_TITLE_LENGTH, message = "제목은 최대 ${MAX_TITLE_LENGTH}자까지 가능합니다.")
    val title: String? = null,

    @Schema(description = "콘텐츠 본문. 본문은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.", example = "어제 함께 갔던 맛집들")
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH, message = "본문은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.")
    val description: String? = null,

    @Schema(
        description = "완료 여부",
        requiredMode = NOT_REQUIRED,
    )
    val isCompleted: Boolean = false,

    @Schema(
        description = "태그 번호 리스트",
        requiredMode = NOT_REQUIRED,
    )
    val tags: List<Long> = emptyList(),

    @Schema(description = "소유자 타입 (ME: 나, PARTNER: 상대방, US: 우리)")
    val ownerType: ContentOwnerType,
) {
    fun toVo(): CreateContentRequestVo {
        return CreateContentRequestVo(
            title = this.title,
            description = this.description,
            isCompleted = this.isCompleted,
            tags = this.tags,
            ownerType = this.ownerType
        )
    }
}

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
    val endTimezone: String? = null,
) {
    fun toVo(): DateTimeInfoVo {
        return DateTimeInfoVo(
            startDateTime = this.startDateTime,
            startTimezone = this.startTimezone,
            endDateTime = this.endDateTime,
            endTimezone = this.endTimezone
        )
    }
}
