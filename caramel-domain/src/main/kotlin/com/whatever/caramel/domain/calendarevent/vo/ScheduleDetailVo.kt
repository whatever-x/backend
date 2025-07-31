package com.whatever.caramel.domain.calendarevent.vo

import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.content.vo.fromRequestorPerspective
import java.time.LocalDateTime

data class ScheduleDetailVo(
    val scheduleId: Long,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val startDateTimezone: String,
    val endDateTimezone: String,
    val isCompleted: Boolean,
    val parentScheduleId: Long? = null,
    val title: String?,
    val description: String?,
    val contentAssignee: ContentAssignee,
) {
    companion object {
        fun from(scheduleEvent: ScheduleEvent): ScheduleDetailVo {
            return ScheduleDetailVo(
                scheduleId = scheduleEvent.id,
                startDateTime = scheduleEvent.startDateTime,
                endDateTime = scheduleEvent.endDateTime,
                startDateTimezone = scheduleEvent.startTimeZone.id,
                endDateTimezone = scheduleEvent.endTimeZone.id,
                isCompleted = scheduleEvent.content.contentDetail.isCompleted,
                title = scheduleEvent.content.contentDetail.title,
                description = scheduleEvent.content.contentDetail.description,
                contentAssignee = scheduleEvent.content.contentAssignee,
            )
        }

        /**
         * N+1 방지를 위해 명시적으로 content 입력받았습니다.
         */
        fun from(scheduleEvent: ScheduleEvent, content: Content): ScheduleDetailVo {
            return ScheduleDetailVo(
                scheduleId = scheduleEvent.id,
                startDateTime = scheduleEvent.startDateTime,
                endDateTime = scheduleEvent.endDateTime,
                startDateTimezone = scheduleEvent.startTimeZone.id,
                endDateTimezone = scheduleEvent.endTimeZone.id,
                isCompleted = content.contentDetail.isCompleted,
                title = content.contentDetail.title,
                description = content.contentDetail.description,
                contentAssignee = content.contentAssignee,
            )
        }

        fun from(scheduleEvent: ScheduleEvent, content: Content, requestUserId: Long): ScheduleDetailVo {
            val isContentOwnerSameAsRequester = content.user.id == requestUserId
            return ScheduleDetailVo(
                scheduleId = scheduleEvent.id,
                startDateTime = scheduleEvent.startDateTime,
                endDateTime = scheduleEvent.endDateTime,
                startDateTimezone = scheduleEvent.startTimeZone.id,
                endDateTimezone = scheduleEvent.endTimeZone.id,
                isCompleted = content.contentDetail.isCompleted,
                title = content.contentDetail.title,
                description = content.contentDetail.description,
                contentAssignee = content.contentAssignee.fromRequestorPerspective(isContentOwnerSameAsRequester),
            )
        }
    }
}

data class ScheduleDetailsVo(
    val scheduleDetailVoList: List<ScheduleDetailVo>,
) {
    companion object {
        fun from(coupleSchedules: List<ScheduleEvent>): ScheduleDetailsVo {
            return ScheduleDetailsVo(
                coupleSchedules.map { ScheduleDetailVo.from(it) }
            )
        }

        fun from(coupleSchedules: List<ScheduleEvent>, requestUserId: Long): ScheduleDetailsVo {
            return ScheduleDetailsVo(
                coupleSchedules.map { ScheduleDetailVo.from(it, it.content, requestUserId) }
            )
        }
    }
}
