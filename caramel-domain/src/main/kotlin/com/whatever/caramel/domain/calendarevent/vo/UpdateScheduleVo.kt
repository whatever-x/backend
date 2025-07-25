package com.whatever.caramel.domain.calendarevent.vo

import com.whatever.caramel.domain.content.model.ContentDetail.Companion.MAX_DESCRIPTION_LENGTH
import com.whatever.caramel.domain.content.vo.ContentAssignee
import org.hibernate.validator.constraints.CodePointLength
import java.time.LocalDate
import java.time.LocalDateTime

data class UpdateScheduleVo(
    val selectedDate: LocalDate,
    val title: String? = null,
    @field:CodePointLength(max = MAX_DESCRIPTION_LENGTH, message = "설명은 최대 ${MAX_DESCRIPTION_LENGTH}자까지 가능합니다.")
    val description: String? = null,
    val isCompleted: Boolean,
    val startDateTime: LocalDateTime? = null,
    val startTimeZone: String? = null,
    val endDateTime: LocalDateTime? = null,
    val endTimeZone: String? = null,
    val tagIds: Set<Long> = emptySet(),
    val contentAsignee: ContentAssignee,
) {
    companion object {
        fun from(
            selectedDate: LocalDate,
            title: String? = null,
            description: String?,
            isCompleted: Boolean,
            startDateTime: LocalDateTime? = null,
            startTimeZone: String? = null,
            endDateTime: LocalDateTime? = null,
            endTimeZone: String? = null,
            tagIds: Set<Long> = emptySet(),
            contentAsignee: ContentAssignee,
        ): UpdateScheduleVo {
            return UpdateScheduleVo(
                selectedDate = selectedDate,
                title = title,
                description = description,
                isCompleted = isCompleted,
                startDateTime = startDateTime,
                startTimeZone = startTimeZone,
                endDateTime = endDateTime,
                endTimeZone = endTimeZone,
                tagIds = tagIds,
                contentAsignee = contentAsignee,
            )
        }
    }
}
