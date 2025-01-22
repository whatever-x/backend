package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.base.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class EventRecurrence (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RecurrenceType,

    @Column(nullable = false)
    var step: Int = 1,

    @Enumerated(EnumType.STRING)
    var monthlyAnnuallyOption: MonthlyAnnuallyOption? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var endCondition: RecurrenceEndCondition = RecurrenceEndCondition.N_REPETITIONS,

    var untilDate: LocalDate? = null,

    @Column(nullable = false)
    var reps: Int = 0,

    // WEEKLY 반복일 경우에 날짜가 자주 사용된다면 양방향 매핑
) : BaseEntity() {
}