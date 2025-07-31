// package com.whatever.caramel.api.calendarevent.eventrecurrence.model
//
// import com.whatever.caramel.domain.base.BaseEntity
// import com.whatever.caramel.domain.calendarevent.scheduleevent.model.ScheduleEvent
// import com.whatever.caramel.domain.calendarevent.scheduleevent.model.converter.ZonedIdConverter
// import com.whatever.caramel.domain.content.model.ContentDetail
// import jakarta.persistence.Column
// import jakarta.persistence.Convert
// import jakarta.persistence.Embedded
// import jakarta.persistence.Entity
// import jakarta.persistence.EnumType
// import jakarta.persistence.Enumerated
// import jakarta.persistence.FetchType
// import jakarta.persistence.GeneratedValue
// import jakarta.persistence.GenerationType
// import jakarta.persistence.Id
// import jakarta.persistence.JoinColumn
// import jakarta.persistence.ManyToOne
// import java.time.LocalDateTime
// import java.time.ZoneId
//
// @Entity
// class ScheduleRecurrenceOverride(
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     val id: Long = 0L,
//
//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false)
//     var type: RecurrenceOverrideType,
//
//     @Column(name = "origin_start_datetime", nullable = false)
//     var originStartDatetime: LocalDateTime,
//
//     var startDatetime: LocalDateTime? = null,
//
//     var endDatetime: LocalDateTime? = null,
//
//     @Column(nullable = false)
//     @Convert(converter = ZonedIdConverter::class)
//     var startTimezone: ZoneId? = null,
//
//     @Column(nullable = false)
//     @Convert(converter = ZonedIdConverter::class)
//     var endTimezone: ZoneId? = null,
//
//     @Embedded
//     var contentDetail: ContentDetail,
//
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "event_id", referencedColumnName = "id")
//     val scheduleEvent: ScheduleEvent? = null,
// ) : BaseEntity()
