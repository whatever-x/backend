package com.whatever.domain.calendarevent.eventrecurrence.model

import com.whatever.domain.calendarevent.eventrecurrence.model.converter.RecurrenceDaySetConverter
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

/**
 * **Event의 반복(Recurrence) 규칙**을 표현하는 객체입니다.
 *
 * 이 클래스는 **RFC 5545의 iCalendar 반복 규칙**을 기반으로 적용했습니다.
 *
 * ## 반복 유형별 옵션
 *
 * **1. 매일 (Daily)**
 * - 이벤트가 매일 반복됩니다.
 *
 * **2. 매주 (Weekly)**
 * - 이벤트가 매주 반복되며, 특정 요일에만 발생합니다.
 * - **필수 인수:**
 *   - **byDay:** 반복 대상 요일의 집합
 *     예: `{RecurrenceDay(MO), RecurrenceDay(WE), RecurrenceDay(FR)}`
 *
 * **3. 매월 (Monthly)**
 * - 이벤트가 매월 반복됩니다.
 * - 반복 옵션은 두 가지 중 하나로 설정합니다:
 *   - **(a) 월의 특정 일자로 반복:**
 *     예: 매월 15일
 *     - **필수 인수:** `byMonthDay` (정수, 예: 15)
 *   - **(b) 월의 특정 요일(ordinal 포함)으로 반복:**
 *     예: 매월 첫 번째 월요일
 *     - **필수 인수:** `byDay` (RecurrenceDay, 예: `RecurrenceDay(MO, 1)`)
 *
 * **4. 매년 (Yearly)**
 * - 이벤트가 매년 반복됩니다.
 * - 반복 옵션은 두 가지 중 하나로 설정합니다:
 *   - **(a) 연간 동일 월의 특정 일자로 반복:**
 *     예: 매년 15일
 *     - **필수 인수:** `byMonthDay` (정수, 예: 15)
 *   - **(b) 연간 동일 월의 특정 요일(ordinal 포함)으로 반복:**
 *     예: 매년 첫 번째 월요일
 *     - **필수 인수:** `byDay` (RecurrenceDay, 예: `RecurrenceDay(MO, 1)`)
 *
 * ## 공통 옵션
 * - **until:** 반복 종료 시점 (`LocalDateTime`), 지정되면 `count`는 무시됩니다.
 * - **count:** 반복 발생 횟수, `until`이 지정된 경우 자동으로 `null` 처리됩니다.
 */
@Embeddable
class EventRecurrence(
    @Enumerated(EnumType.STRING)
    var frequency: RecurrenceFrequency,

    var interval: Int = 1,

    var until: LocalDateTime? = null,

    var count: Int? = null,

    @Convert(converter = RecurrenceDaySetConverter::class)
    var byDay: Set<RecurrenceDay>? = emptySet(),

    var byMonthDay: Int? = null,
) {

    companion object {

        /**
         * 매일 반복되는 이벤트를 생성합니다.
         *
         * @param interval 반복 간격 (일 단위, 기본값: 1)
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun daily(
            interval: Int = 1,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            return EventRecurrence(
                frequency = RecurrenceFrequency.DAILY,
                interval = interval,
                until = until,
                count = if (until == null) count else null
            )
        }

        /**
         * 매주 반복되는 이벤트를 생성합니다.
         *
         * @param interval 반복 간격 (주 단위, 기본값: 1)
         * @param byDay 반복 대상 요일의 집합 (예: {RecurrenceDay(MO), RecurrenceDay(WE)})
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun weekly(
            interval: Int = 1,
            byDay: Set<RecurrenceDay>,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            require(byDay.isNotEmpty()) { "Weekly recurrence must have at least one day specified." }
            return EventRecurrence(
                frequency = RecurrenceFrequency.WEEKLY,
                interval = interval,
                until = until,
                count = if (until == null) count else null,
                byDay = byDay
            )
        }

        /**
         * 매월 반복되는 이벤트를 생성합니다. (월의 특정 일자로 반복)
         *
         * @param interval 반복 간격 (월 단위, 기본값: 1)
         * @param byMonthDay 반복할 일자 (예: 15)
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun monthly(
            interval: Int = 1,
            byMonthDay: Int,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            require(byMonthDay in 1..31)
            return EventRecurrence(
                frequency = RecurrenceFrequency.MONTHLY,
                interval = interval,
                until = until,
                count = if (until == null) count else null,
                byMonthDay = byMonthDay
            )
        }

        /**
         * 매월 반복되는 이벤트를 생성합니다. (월의 n번째 m요일로 반복)
         *
         * @param interval 반복 간격 (월 단위, 기본값: 1)
         * @param byDay 반복 대상 요일(ordinal 포함)을 전달 (예: 첫 번째 월요일 -> RecurrenceDay(MO, 1))
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun monthly(
            interval: Int = 1,
            byDay: RecurrenceDay,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            return EventRecurrence(
                frequency = RecurrenceFrequency.MONTHLY,
                interval = interval,
                until = until,
                count = if (until == null) count else null,
                byDay = setOf(byDay)
            )
        }

        /**
         * 매년 반복되는 이벤트를 생성합니다. (년간 동일 월의 특정 일자로 반복)
         *
         * @param interval 반복 간격 (년 단위, 기본값: 1)
         * @param byMonthDay 반복할 일자 (예: 15)
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun yearly(
            interval: Int = 1,
            byMonthDay: Int,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            require(byMonthDay in 1..31)
            return EventRecurrence(
                frequency = RecurrenceFrequency.YEARLY,
                interval = interval,
                until = until,
                count = if (until == null) count else null,
                byMonthDay = byMonthDay
            )
        }

        /**
         * 매년 반복되는 이벤트를 생성합니다. (년간 동일 월의 n번째 m요일로 반복)
         *
         * @param interval 반복 간격 (년 단위, 기본값: 1)
         * @param byDay 반복 대상 요일(ordinal 포함)을 전달 (예: 첫 번째 월요일 -> RecurrenceDay(MO, 1))
         * @param until 반복 종료 시점 (선택), 지정되면 count는 무시됩니다
         * @param count 반복 횟수 (선택), until이 지정된 경우 자동으로 null 처리됩니다
         */
        fun yearly(
            interval: Int = 1,
            byDay: RecurrenceDay,
            until: LocalDateTime? = null,
            count: Int? = null,
        ): EventRecurrence {
            return EventRecurrence(
                frequency = RecurrenceFrequency.YEARLY,
                interval = interval,
                until = until,
                count = if (until == null) count else null,
                byDay = setOf(byDay)
            )
        }
    }
}
