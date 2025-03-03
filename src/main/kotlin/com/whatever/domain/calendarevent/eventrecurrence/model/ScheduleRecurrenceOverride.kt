package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
import com.whatever.domain.content.model.ContentDetail
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class ScheduleRecurrenceOverride(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RecurrenceOverrideType,

    @Column(name = "origin_start_datetime", nullable = false)
    var originStartDatetime: LocalDateTime,

    var startDatetime: LocalDateTime? = null,

    var endDatetime: LocalDateTime? = null,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var startTimezone: ZoneId? = null,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var endTimezone: ZoneId? = null,

    @Embedded
    var contentDetail: ContentDetail,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    val scheduleEvent: ScheduleEvent? = null,
) : BaseEntity()
