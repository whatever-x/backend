package com.whatever.domain.content.vo

import com.whatever.domain.calendarevent.model.ScheduleEvent
import java.time.LocalDateTime
import java.time.ZoneId

data class ScheduleEventVo(
    val id: Long,
    val uid: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val startTimeZone: ZoneId,
    val endTimeZone: ZoneId,
    val content: ContentVo,
) {
    companion object {
        fun from(scheduleEvent: ScheduleEvent): ScheduleEventVo {
            return ScheduleEventVo(
                id = scheduleEvent.id,
                uid = scheduleEvent.uid,
                startDateTime = scheduleEvent.startDateTime,
                endDateTime = scheduleEvent.endDateTime,
                startTimeZone = scheduleEvent.startTimeZone,
                endTimeZone = scheduleEvent.endTimeZone,
                content = ContentVo.from(scheduleEvent.content)
            )
        }
    }
} 
