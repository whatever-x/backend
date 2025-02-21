package com.whatever.domain.calendarevent.taskevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.content.model.ContentDetails
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class TaskEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val uid: String,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Embedded
    val contentDetails: ContentDetails,

    @Column(nullable = false)
    var isCompleted: Boolean = false,

    @Embedded
    var eventRecurrence: EventRecurrence,
) : BaseEntity() {
}