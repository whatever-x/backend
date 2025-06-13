package com.whatever.domain.calendarevent.scheduleevent.controller.dto

import com.whatever.domain.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.content.controller.dto.response.TagDto
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.tag.model.Tag

data class GetScheduleResponse(
    val scheduleDetail: ScheduleDetailDto,
    val tags: List<TagDto> = emptyList()
) {
    companion object {
        fun of(
            schedule: ScheduleEvent,
            content: Content,
            tags: List<Tag>,
        ) = GetScheduleResponse(
            scheduleDetail = ScheduleDetailDto(
                scheduleId = schedule.id,
                startDateTime = schedule.startDateTime,
                endDateTime = schedule.endDateTime,
                startDateTimezone = schedule.startTimeZone.id,
                endDateTimezone = schedule.endTimeZone.id,
                isCompleted = content.contentDetail.isCompleted,
                title = content.contentDetail.title,
                description = content.contentDetail.description,
            ),
            tags = tags.map { TagDto.from(it) }
        )
    }
}
