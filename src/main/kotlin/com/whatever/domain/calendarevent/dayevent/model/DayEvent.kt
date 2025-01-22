package com.whatever.domain.calendarevent.dayevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.content.model.Content
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class DayEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id", nullable = false)
    val content: Content,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(nullable = false)
    val isTodo: Boolean = false,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_id", referencedColumnName = "id")
    val eventRecurrence: EventRecurrence? = null,

    @OneToMany(mappedBy = "dayEvent")
    val daySlots: MutableList<DaySlot> = mutableListOf(),
) : BaseEntity() {

    fun addSlots(daySlots: List<DaySlot>) {
        this.daySlots.addAll(daySlots)
    }

}