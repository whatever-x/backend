package com.whatever.domain.calendarevent.scheduleevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.content.model.ContentDetails
import com.whatever.domain.timezone.TimeZone
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class ScheduleEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val uid: String,

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

    @Embedded
    var contentDetails: ContentDetails,

    var isCompleted: Boolean,
) : BaseEntity() {
}