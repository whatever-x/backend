package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.content.model.ContentDetails
import com.whatever.domain.timezone.TimeZone
import jakarta.persistence.*
import java.time.LocalDateTime

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

    @OneToOne
    @JoinColumn(name = "start_timezone_id", referencedColumnName = "id", nullable = false)
    var startTimezone: TimeZone? = null,

    @OneToOne
    @JoinColumn(name = "end_timezone_id", referencedColumnName = "id", nullable = false)
    var endTimezone: TimeZone? = null,

    @Embedded
    var contentDetails: ContentDetails,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    val scheduleEvent: ScheduleEvent? = null,
) : BaseEntity()
