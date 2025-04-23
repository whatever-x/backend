package com.whatever.domain.calendarevent.scheduleevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.util.endOfDay
import com.whatever.util.toZonId
import com.whatever.util.withoutNano
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Version
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class ScheduleEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val uid: String,

    startDateTime: LocalDateTime,

    endDateTime: LocalDateTime,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var startTimeZone: ZoneId,

    @Column(nullable = false)
    @Convert(converter = ZonedIdConverter::class)
    var endTimeZone: ZoneId,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    var content: Content,

//    @Embedded
//    var eventRecurrence: EventRecurrence? = null,
//    @OneToMany(mappedBy = "scheduleEvent", fetch = FetchType.LAZY)
//    val recurrenceOverrides: MutableList<ScheduleRecurrenceOverride> = mutableListOf(),
) : BaseEntity() {

    @Column(nullable = false)
    var startDateTime: LocalDateTime = startDateTime.withoutNano
        set(value) {
            field = value.withoutNano
        }

    @Column(nullable = false)
    var endDateTime: LocalDateTime = endDateTime.withoutNano
        set(value) {
            field = value.withoutNano
        }

    @Version
    private var version: Long = 0L

    fun updateDuration(
        startDateTime: LocalDateTime,
        startTimeZone: String,
        endDateTime: LocalDateTime? = null,
        endTimeZone: String? = null,
    ) {
        this.startDateTime = startDateTime.withoutNano
        this.startTimeZone = startTimeZone.toZonId()
        this.endDateTime = endDateTime ?: startDateTime.endOfDay.withoutNano
        this.endTimeZone = endTimeZone?.toZonId() ?: startTimeZone.toZonId()
    }

    fun updateEvent(
        contentDetail: ContentDetail,
        startDateTime: LocalDateTime,
        startTimeZone: String,
        endDateTime: LocalDateTime? = null,
        endTimeZone: String? = null,
    ) {
        content.updateContentDetail(contentDetail)

        updateDuration(
            startDateTime = startDateTime,
            startTimeZone = startTimeZone,
            endDateTime = endDateTime,
            endTimeZone = endTimeZone,
        )
    }

    fun convertToMemo(contentDetail: ContentDetail) {
        content.updateContentDetail(contentDetail)
        content.updateType(ContentType.MEMO)
        this.deleteEntity()
    }
}