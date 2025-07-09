package com.whatever.domain.calendarevent.scheduleevent.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_CONTENT_TYPE
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode.ILLEGAL_DURATION
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
import com.whatever.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.global.exception.ErrorUi
import com.whatever.util.endOfDay
import com.whatever.util.toZoneId
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
import java.util.UUID

@Entity
class ScheduleEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val uid: String,

    startDateTime: LocalDateTime,

    endDateTime: LocalDateTime,

    @Column(nullable = false, length = MAX_TIME_ZONE_LENGTH)
    @Convert(converter = ZonedIdConverter::class)
    var startTimeZone: ZoneId,

    @Column(nullable = false, length = MAX_TIME_ZONE_LENGTH)
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

    init {
        val startInstant = startDateTime.atZone(startTimeZone).toInstant()
        val endInstant   = endDateTime.atZone(endTimeZone).toInstant()

        if (endInstant.isBefore(startInstant)) {
            throw ScheduleIllegalArgumentException(
                errorCode = ILLEGAL_DURATION,
                errorUi = ErrorUi.Toast("시작일은 종료일보다 이전이어야 해요."),
            )
        }
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
        this.startTimeZone = startTimeZone.toZoneId()
        this.endDateTime = endDateTime ?: startDateTime.endOfDay.withoutNano
        this.endTimeZone = endTimeZone?.toZoneId() ?: startTimeZone.toZoneId()
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

    companion object {
        const val MAX_TIME_ZONE_LENGTH = 50

        fun fromMemo(
            memo: Content,
            startDateTime: LocalDateTime,
            startTimeZone: ZoneId,
            endDateTime: LocalDateTime? = null,
            endTimeZone: ZoneId? = null,
        ): ScheduleEvent {
            if (memo.type != ContentType.MEMO) {
                throw ScheduleIllegalArgumentException(
                    errorCode = ILLEGAL_CONTENT_TYPE,
                    errorUi = ErrorUi.Toast("메모 콘텐츠만 일정으로 바꿀 수 있어요.")
                )
            }

            memo.updateType(ContentType.SCHEDULE)
            return ScheduleEvent(
                content = memo,
                uid = UUID.randomUUID().toString(),
                startDateTime = startDateTime,
                startTimeZone = startTimeZone,
                endDateTime = endDateTime ?: startDateTime.endOfDay.withoutNano,
                endTimeZone = endTimeZone ?: startTimeZone,
            )
        }
    }
}