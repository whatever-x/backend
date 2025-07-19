package com.whatever.domain.calendarevent.vo

import com.whatever.domain.calendarevent.model.ScheduleEvent
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
    }
}
