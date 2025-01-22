package com.whatever.domain.calendarevent.dayevent.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class DaySlot (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_event_id", referencedColumnName = "id", nullable = false)
    val dayEvent: DayEvent,

    @Column(nullable = false)
    var slotDate: LocalDate,

    @Column(nullable = false)
    var isCompleted: Boolean = false,
) : BaseEntity() {
}