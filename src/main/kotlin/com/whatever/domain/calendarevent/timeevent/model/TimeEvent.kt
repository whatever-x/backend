package com.whatever.domain.calendarevent.timeevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.content.model.Content
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.timezone.TimeZone
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class TimeEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    val content: Content,

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_id", referencedColumnName = "id")
    val eventRecurrence: EventRecurrence? = null,

    @OneToMany(mappedBy = "timeEvent")
    val timeSlots: MutableList<TimeSlot> = mutableListOf(),
) : BaseEntity() {

    fun addSlots(timeSlots: List<TimeSlot>) {
        this.timeSlots.addAll(timeSlots)
    }

}