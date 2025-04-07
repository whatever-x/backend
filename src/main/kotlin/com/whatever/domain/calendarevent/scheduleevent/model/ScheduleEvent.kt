package com.whatever.domain.calendarevent.scheduleevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.calendarevent.eventrecurrence.model.ScheduleRecurrenceOverride
import com.whatever.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
import com.whatever.domain.content.model.ContentDetail
import com.whatever.util.endOfDay
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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
    var startDateTime: LocalDateTime,

    @Column(nullable = false)
    var endDateTime: LocalDateTime,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var startTimeZone: ZoneId,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var endTimeZone: ZoneId,

    @Embedded
    var contentDetail: ContentDetail,

    @Embedded
    var eventRecurrence: EventRecurrence? = null,

    @OneToMany(mappedBy = "scheduleEvent", fetch = FetchType.LAZY)
    val recurrenceOverrides: MutableList<ScheduleRecurrenceOverride> = mutableListOf(),
) : BaseEntity() {

    fun updateDuration(
        startDateTime: LocalDateTime,
        startTimeZone: ZoneId,
        endDateTime: LocalDateTime = startDateTime.endOfDay,
        endTimeZone: ZoneId = startTimeZone,
    ) {
        this.startDateTime = startDateTime
        this.startTimeZone = startTimeZone
        this.endDateTime = endDateTime
        this.endTimeZone = endTimeZone
    }
}