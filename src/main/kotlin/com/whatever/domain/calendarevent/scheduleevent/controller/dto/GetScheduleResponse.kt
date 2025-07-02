package com.whatever.domain.calendarevent.scheduleevent.controller.dto

import com.whatever.domain.calendarevent.controller.dto.response.ScheduleDetailDto
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.content.controller.dto.response.TagDto
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.tag.model.Tag
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "일정 조회 응답 DTO")
data class GetScheduleResponse(
    @Schema(description = "일정 상세 정보")
    val scheduleDetail: ScheduleDetailDto,
    @Schema(description = "연관 태그 리스트")
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
