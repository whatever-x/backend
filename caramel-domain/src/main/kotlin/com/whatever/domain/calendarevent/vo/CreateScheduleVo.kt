package com.whatever.domain.calendarevent.vo

import com.whatever.domain.content.model.ContentDetail.Companion.MAX_DESCRIPTION_LENGTH
import com.whatever.domain.content.model.ContentDetail.Companion.MAX_TITLE_LENGTH
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDateTime

data class CreateScheduleVo(
    @field:CodePointLength(max = MAX_TITLE_LENGTH, message = "제목은 최대 ${MAX_TITLE_LENGTH}자까지 가능합니다.")
    val title: String? = null,
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH, message = "설명은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.")
    val description: String? = null,
    val isCompleted: Boolean = false,
    val startDateTime: LocalDateTime,
    val startTimeZone: String,
    val endDateTime: LocalDateTime? = null,
    val endTimeZone: String? = null,
    val tagIds: Set<Long> = emptySet(),
) {
    companion object {
        fun from(
            title: String? = null,
            description: String? = null,
            isCompleted: Boolean = false,
            startDateTime: LocalDateTime,
            startTimeZone: String,
            endDateTime: LocalDateTime? = null,
            endTimeZone: String? = null,
            tagIds: Set<Long> = emptySet(),
        ): CreateScheduleVo {
            return CreateScheduleVo(
                title = title,
                description = description,
                isCompleted = isCompleted,
                startDateTime = startDateTime,
                startTimeZone = startTimeZone,
                endDateTime = endDateTime,
                endTimeZone = endTimeZone,
                tagIds = tagIds,
            )
        }
    }
}
