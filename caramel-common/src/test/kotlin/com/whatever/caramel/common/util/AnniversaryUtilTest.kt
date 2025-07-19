package com.whatever.caramel.common.util

import com.whatever.caramel.common.util.AnniversaryUtil.findNThDayAnniversary
import com.whatever.caramel.common.util.AnniversaryUtil.findYearlyAnniversary
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit.DAYS
import kotlin.test.Test

class AnniversaryUtilTest {

    @DisplayName("조회 시작일이 대상일 이전이어도, 대상일부터 계산한다")
    @Test
    fun findNThDayAnniversary_WithSearchStartBeforeTarget() {
        // given
        val targetDate = LocalDate.of(2025, 1, 10)
        val searchStart = LocalDate.of(2025, 1, 1)   // target 이전
        val searchEnd = targetDate.plusDays(50)

        // when
        val result = findNThDayAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            interval = 20,
        )

        // then
        assertThat(result.map { it.date })
            .containsExactly(
                LocalDate.of(2025, 1, 29),
                LocalDate.of(2025, 2, 18),
            )
    }

    @DisplayName("interval=1 이면, 매일이 기념일로 반환된다")
    @Test
    fun findNThDayAnniversary_WithDailyInterval() {
        // given
        val targetDate = LocalDate.of(2025, 1, 1)
        val searchEnd = targetDate.plusDays(2) // 01.01 ~ 01.03 조회

        // when
        val res = findNThDayAnniversary(
            targetDate = targetDate,
            startDate = targetDate,
            endDate = searchEnd,
            interval = 1,
        )

        // then
        assertThat(res.map { it.date })
            .containsExactly(
                targetDate.plusDays(0),
                targetDate.plusDays(1),
                targetDate.plusDays(2),
            )
    }

    @DisplayName("조회 범위에 포함된 30일 단위 기념일을 반환한다.")
    @Test
    fun findNThDayAnniversary_WithInterval30() {
        // given
        val targetDate = LocalDate.of(2025, 1, 1)
        val searchStart = targetDate
        val searchEnd = searchStart.plusDays(90)
        val interval = 30

        val expectedDates = mutableListOf<LocalDate>()
        var cursor = searchStart.minusDays(1)
        while (cursor.plusDays(interval.toLong()) <= searchEnd) {
            cursor = cursor.plusDays(interval.toLong())
            expectedDates.add(cursor)
        }

        // when
        val result = findNThDayAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            interval = interval,
        )

        // then
        assertThat(result.map { it.date }).containsExactlyInAnyOrderElementsOf(expectedDates)

        assertThat(result.map { it.nTh })
            .containsExactlyInAnyOrderElementsOf(
                expectedDates.map { 1 + DAYS.between(targetDate, it).toInt() }
            )
    }

    @DisplayName("interval=365 여도, 윤년(366일) 보정 없이 계산된다.")
    @ParameterizedTest
    @CsvSource(
        "2024, 2024-12-30",
        "2025, 2025-12-31",
    )
    fun findNThDayAnniversary_WithInterval365(year: Int, expectedDate: LocalDate) {
        // given
        val targetDate = LocalDate.of(year, 1, 1)
        val interval = 365
        val searchStart = targetDate
        val searchEnd = searchStart.plusYears(1)  // 1년 조회

        val expectedNTh = (1 + DAYS.between(targetDate, expectedDate)).toInt()

        // when
        val result = findNThDayAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            interval = interval,
        )

        // then
        assertThat(result.map { it.date to it.nTh })
            .containsExactly(
                expectedDate to expectedNTh,
            )
    }

    @DisplayName("검색 범위가 겹치지 않으면 빈 리스트를 반환한다.")
    @Test
    fun findNThDayAnniversary_WithSearchEndBeforeSearchStart() {
        // given
        val targetDate = LocalDate.of(2025, 1, 1)
        val searchStart = targetDate.plusDays(10)
        val searchEnd = targetDate.plusDays(9)  // searchStart > searchEnd

        // when
        val result = findNThDayAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            interval = 5,
        )

        // then
        assertThat(result).isEmpty()
    }

    @DisplayName("일반 날짜에 대해 기본 주년 기념일이 반환된다.")
    @Test
    fun findYearlyAnniversary_success() {
        // given
        val targetDate = LocalDate.of(2025, 1, 1)
        val searchStart = targetDate
        val searchEnd = searchStart.plusYears(2)

        // when
        val result = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
        )

        // then
        assertThat(result.map { it.date to it.nTh to it.isAdjustedForNonLeapYear })
            .containsExactly(
                targetDate.plusYears(1) to 1 to false,
                targetDate.plusYears(2) to 2 to false,
            )
    }

    @DisplayName("윤년 2월 29일에 대해 기본 보정(2월 28일)이 적용된다.")
    @Test
    fun findYearlyAnniversary_2_WithFeb29AdjustedToDefault() {
        // given
        val targetDate = LocalDate.of(2024, 2, 29)
        val searchStart = targetDate
        val searchEnd = searchStart.plusYears(4)

        // when
        val result = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
        )

        // then
        assertThat(result.map { it.date to it.nTh to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2025, 2, 28) to 1 to true,
                LocalDate.of(2026, 2, 28) to 2 to true,
                LocalDate.of(2027, 2, 28) to 3 to true,
                LocalDate.of(2028, 2, 29) to 4 to false,
            )
    }

    @DisplayName("보정을 nul로 하면 평년 기념일은 건너뛴다.")
    @Test
    fun findYearlyAnniversary_2_WithFeb29AdjustedToNull() {
        // given
        val targetDate = LocalDate.of(2024, 2, 29)
        val searchStart = targetDate
        val searchEnd = searchStart.plusYears(4)

        // when
        val result = findYearlyAnniversary(
            targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            feb29InNonLeapYearAdjust = null,
        )

        // then
        assertThat(result.map { it.date to it.nTh to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2028, 2, 29) to 4 to false,
            )
    }

    @DisplayName("사용자 지정 보정일(3월 1일)이 적용된다.")
    @Test
    fun findYearlyAnniversary_2_WithFeb29AdjustedToMarch01() {
        // given
        val targetDate = LocalDate.of(2024, 2, 29)
        val searchStart = targetDate
        val searchEnd = searchStart.plusYears(4)
        val customAdjust = MonthDay.of(3, 1)

        // when
        val result = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = searchStart,
            endDate = searchEnd,
            feb29InNonLeapYearAdjust = customAdjust,
        )

        // then
        assertThat(result.map { it.date to it.nTh to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2025, 3, 1) to 1 to true,
                LocalDate.of(2026, 3, 1) to 2 to true,
                LocalDate.of(2027, 3, 1) to 3 to true,
                LocalDate.of(2028, 2, 29) to 4 to false,
            )
    }

    @DisplayName("검색 범위가 targetDate의 1주년보다 이전이면 빈 리스트를 반환한다.")
    @Test
    fun findYearlyAnniversary_WithSearchEndBeforeFirstYearAnniversary2() {
        // given
        val targetDate = LocalDate.of(2025, 1, 1)
        val searchEnd = targetDate
            .plusYears(1)  // 1주년보다
            .minusDays(1)  // 하루 이전

        // when
        val result = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = targetDate,
            endDate = searchEnd,
            feb29InNonLeapYearAdjust = MonthDay.of(2, 28),
        )

        // then
        assertThat(result).isEmpty()
    }

    @DisplayName("illegal feb29Adjust(2/29) 입력 시에도, 2/28로 자동 보정")
    @Test
    fun findYearlyAnniversary_2_WithIllegalFeb29Adjust() {
        // given
        val targetDate = LocalDate.of(2024, 2, 29)
        val searchEnd = targetDate.plusYears(4)

        // when
        val result = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = targetDate,
            endDate = searchEnd,
            feb29InNonLeapYearAdjust = MonthDay.of(2, 29),  // illegal
        )

        // then
        // 기존 테스트와 동일하게 2025~2027은 2/28, 2028은 2/29
        assertThat(result.map { it.date })
            .containsExactly(
                LocalDate.of(2025, 2, 28),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2027, 2, 28),
                LocalDate.of(2028, 2, 29),
            )
    }

    @DisplayName("targetDate이 2월 29일이 아닐 때, 윤년 보정값을 전달해도 반영되지 않는다.")
    @Test
    fun findYearlyAnniversary_2_WithNonFeb29WithAdjustParam() {
        // given
        val targetDate = LocalDate.of(2025, 2, 28)
        val searchEdn = targetDate.plusYears(2)

        // when
        val res = findYearlyAnniversary(
            targetDate = targetDate,
            startDate = targetDate,
            endDate = searchEdn,
            feb29InNonLeapYearAdjust = MonthDay.of(3, 1),
        )

        // then
        // 단순히 1주년·2주년: 2026-02-28, 2027-02-28
        assertThat(res.map { it.date })
            .containsExactly(
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2027, 2, 28),
            )
    }
}
