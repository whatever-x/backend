package com.whatever.domain.calendarevent.scheduleevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.calendarevent.eventrecurrence.model.ScheduleRecurrenceOverride
import com.whatever.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
import com.whatever.domain.content.model.ContentDetail
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class ScheduleEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val uid: String,

    @Column(nullable = false)
    val startDateTime: LocalDateTime,

    @Column(nullable = false)
    val endDateTime: LocalDateTime,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    val startTimeZone: ZoneId,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    val endTimeZone: ZoneId,

    @Embedded
    var contentDetail: ContentDetail,

    @Embedded
    var eventRecurrence: EventRecurrence,

    @OneToMany(mappedBy = "scheduleEvent", fetch = FetchType.LAZY)
    val recurrenceOverrides: MutableList<ScheduleRecurrenceOverride> = mutableListOf(),
) : BaseEntity() {

}