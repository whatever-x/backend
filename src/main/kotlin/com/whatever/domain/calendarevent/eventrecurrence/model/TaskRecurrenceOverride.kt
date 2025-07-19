package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.taskevent.model.TaskEvent
import com.whatever.domain.content.model.ContentDetail
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class TaskRecurrenceOverride(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RecurrenceOverrideType,

    @Column(name = "origin_start_datetime", nullable = false)
    var originStartDate: LocalDate,

    var startDate: LocalDate? = null,

    @Embedded
    var contentDetail: ContentDetail,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    val taskEvent: TaskEvent? = null,
) : BaseEntity()
