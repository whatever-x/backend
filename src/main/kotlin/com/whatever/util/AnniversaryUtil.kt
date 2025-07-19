package com.whatever.util

import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.time.temporal.ChronoUnit.DAYS

object AnniversaryUtil {

    private val FEB_29 = MonthDay.of(2, 29)

    /**
     * targetDate를 포함한 N일 기념일을 찾는 함수
     * 365일을 입력한다고 하더라도 윤년(366일)에 대한 보정을 진행하지 않습니다.
     *
     * @param targetDate 대상일
     * @param startDate 조회 범위 시작일
     * @param endDate 조회 범위 종료일
     * @param interval N일
     */
    fun findNThDayAnniversary(
        targetDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        interval: Int,
    ): List<AnniversaryDto> {
        if (startDate.isAfter(endDate) || endDate.isBefore(targetDate)) {
            return emptyList()
        }

        val effectiveStart = maxOf(startDate, targetDate)
        val daysFromStart = DAYS.between(targetDate, effectiveStart)
        val daysFromEnd = DAYS.between(targetDate, endDate) + 1

        val firstIdx = daysFromStart / interval + 1
        val lastIdx = daysFromEnd / interval

        var annivDate = targetDate
            .minusDays(1)  // targetDate을 포함하여 계산
            .plusDays((firstIdx - 1) * interval)  // 조회 시작일에 맞춰 보정

        return (firstIdx..lastIdx).mapNotNull { idx ->
            annivDate = annivDate.plusDays(interval.toLong())

            AnniversaryDto(
                date = annivDate,
                nTh = (idx * interval).toInt(),
            )
        }
    }

    /**
     * targetDate에 대한 N주년 기념일을 찾는 함수.
     *
     * @param targetDate 대상일
     * @param startDate 조회 범위 시작일
     * @param endDate 조회 범위 종료일
     * @param feb29InNonLeapYearAdjust 기념일이 2월 29일일 경우, 윤년이 아닐 때 대체할 MonthDay. null이면 건너뜀.
     * 만약 이 값으로 2월 29일이 주어질 경우 자동으로 28일로 보정되어 적용
     */
    fun findYearlyAnniversary(
        targetDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        feb29InNonLeapYearAdjust: MonthDay? = MonthDay.of(2, 28),  // null for skip
    ): List<AnniversaryDto> {
        if (startDate.isAfter(endDate) || endDate.isBefore(targetDate)) {
            return emptyList()
        }

        val targetMonthDay = MonthDay.from(targetDate)

        val feb29Adjust = feb29InNonLeapYearAdjust?.let {
            if (it == FEB_29) {  // 보정으로 들어온 날짜가 2월 29일이라면 강제 변경
                MonthDay.of(2, 28)
            } else {
                it
            }
        }

        val isTargetFeb29 = (targetMonthDay == FEB_29)

        val effectiveStartYear = maxOf(startDate.year, targetDate.year + 1)
        return (effectiveStartYear..endDate.year).mapNotNull { year ->
            val isFeb29InNonLeapYear = isTargetFeb29 && !Year.isLeap(year.toLong())

            val adjustedMonthDay = when {
                isFeb29InNonLeapYear -> feb29Adjust ?: return@mapNotNull null  // 타겟이 02.29이고, 윤년이 아닐 때 보정
                else -> targetMonthDay
            }

            val annivDate = adjustedMonthDay.atYear(year)
            if (endDate < annivDate || annivDate < startDate) {
                return@mapNotNull null
            }

            AnniversaryDto(
                date = annivDate,
                nTh = year - targetDate.year,
                isAdjustedForNonLeapYear = isFeb29InNonLeapYear
            )
        }
    }
}

data class AnniversaryDto(
    val date: LocalDate,
    val nTh: Int,
    val isAdjustedForNonLeapYear: Boolean = false,
)

