package com.whatever.domain.calendarevent.timeevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.timezone.TimeZone
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class TimeSlot (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_event_id", referencedColumnName = "id", nullable = false)
    val timeEvent: TimeEvent,

    @Column(nullable = false)
    val startDateTime: LocalDateTime,

    @Column(nullable = false)
    val endDateTime: LocalDateTime,

    @OneToOne
    @JoinColumn(name = "start_timezone_id", referencedColumnName = "id", nullable = false)
    val startTimeZone: TimeZone,

    @OneToOne
    @JoinColumn(name = "end_timezone_id", referencedColumnName = "id", nullable = false)
    val endTimeZone: TimeZone,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
) : BaseEntity() {
}