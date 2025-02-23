package com.whatever.domain.calendarevent.taskevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.eventrecurrence.model.EventRecurrence
import com.whatever.domain.calendarevent.eventrecurrence.model.TaskRecurrenceOverride
import com.whatever.domain.content.model.ContentDetail
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
    val contentDetail: ContentDetail,

    @Embedded
    var eventRecurrence: EventRecurrence,

    @OneToMany(mappedBy = "taskEvent", fetch = FetchType.LAZY)
    val recurrenceOverrides: MutableList<TaskRecurrenceOverride> = mutableListOf(),
) : BaseEntity() {
}