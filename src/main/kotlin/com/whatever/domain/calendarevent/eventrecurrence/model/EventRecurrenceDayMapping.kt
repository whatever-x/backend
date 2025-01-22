package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["event_recurrence_id", "day_of_the_week_id"])
    ]
)
class EventRecurrenceDayMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_recurrence_id", referencedColumnName = "id", nullable = false)
    val eventRecurrence: EventRecurrence,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_of_the_week_id", referencedColumnName = "id", nullable = false)
    val dayOfWeek: DaysOfWeek,
) : BaseEntity() {
}