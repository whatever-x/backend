package com.whatever.caramel.api.calendarevent.specialday.service

import com.whatever.domain.calendarevent.specialday.model.SpecialDay
import com.whatever.domain.calendarevent.specialday.model.SpecialDayType
import com.whatever.domain.calendarevent.specialday.repository.SpecialDayRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SpecialDayServiceTest @Autowired constructor(
    private val specialDayService: SpecialDayService,
    private val specialDayRepository: SpecialDayRepository,
) {

    @DisplayName("휴일 조회 시 해당 월에 존재하는 휴일이 list 형대로 반환된다.")
    @Test
    fun getHolidays() {
        // given
        val startYearMonth = YearMonth.of(2025, 1)
        val endYearMonth = YearMonth.of(2025, 1)
        val currentLocalDate = startYearMonth.atEndOfMonth()
        val holidays = makeTestHoliday(
            count = currentLocalDate.dayOfMonth,  // 1월에만 휴일 설정
            startYear = startYearMonth.year,
        )
        specialDayRepository.saveAll(holidays)

        // when
        val resultWithHolidays = specialDayService.getHolidays(
            startYearMonth,
            endYearMonth
        )
        val resultWithoutHoliday = specialDayService.getHolidays(
            startYearMonth.minusMonths(2),
            startYearMonth.minusMonths(1),
        )

        // then
        assertThat(resultWithHolidays.holidayList).hasSize(currentLocalDate.dayOfMonth)
        assertThat(resultWithoutHoliday.holidayList).isEmpty()
    }

    private fun makeTestHoliday(
        count: Int,
        startYear: Int,
        type: SpecialDayType = SpecialDayType.HOLI,
        setOnlyRestHoliday: Boolean = true,
    ): List<SpecialDay> {
        val holidays = mutableListOf<SpecialDay>()
        var currentDate = LocalDate.of(startYear, 1, 1)
        repeat(count) { idx ->
            val holiday = SpecialDay(
                type = type,
                locDate = currentDate,
                dateName = "Test Holiday ${idx} ($startYear)",
                isHoliday = (setOnlyRestHoliday) || (idx % 2 == 0),
                sequence = 1,
            )
            holidays.add(holiday)
            currentDate = currentDate.plusDays(1)
        }

        return holidays
    }
}
