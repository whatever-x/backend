package com.whatever.domain.calendarevent.vo

import com.whatever.domain.calendarevent.model.ScheduleEvent
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.tag.model.Tag
import java.time.LocalDateTime

data class GetScheduleVo(
    val scheduleId: Long,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val startDateTimezone: String,
    val endDateTimezone: String,
    val isCompleted: Boolean,
    val parentScheduleId: Long? = null,
    val title: String?,
    val description: String?,
    val tags: List<TagVo> = emptyList(),
) {
    companion object {
        fun from(schedule: ScheduleEvent, content: Content, tags: List<Tag>): GetScheduleVo {
            return GetScheduleVo(
                scheduleId = schedule.id,
                startDateTime = schedule.startDateTime,
                endDateTime = schedule.endDateTime,
                startDateTimezone = schedule.startTimeZone.id,
                endDateTimezone = schedule.endTimeZone.id,
                isCompleted = content.contentDetail.isCompleted,
                title = content.contentDetail.title,
                description = content.contentDetail.description,
                tags = tags.map { TagVo.from(it) }
            )
        }
    }
}

data class TagVo(
    val id: Long,
    val label: String,
) {
    companion object {
        fun from(tag: Tag): TagVo {
            return TagVo(
                id = tag.id,
                label = tag.label,
            )
        }
    }
}
