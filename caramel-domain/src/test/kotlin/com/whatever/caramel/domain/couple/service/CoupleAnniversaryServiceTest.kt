package com.whatever.caramel.domain.couple.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.couple.exception.CoupleNotFoundException
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType.BIRTHDAY
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType.N_TH_DAY
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType.YEARLY
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.MonthDay
import kotlin.test.Test

@CaramelDomainSpringBootTest
class CoupleAnniversaryServiceTest @Autowired constructor(
    private val coupleAnniversaryService: CoupleAnniversaryService,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
) {

    @AfterEach
    fun tearDown() {
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("존재하지 않는 커플로 조회하면 예외를 던진다")
    @Test
    fun getCoupleAnniversary_NotFound() {
        assertThrows<CoupleNotFoundException> {
            coupleAnniversaryService.getCoupleAnniversary(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1),
                coupleId = 0L,
                requestUserId = 0L,
            )
        }
    }

    @DisplayName("100일·주년·멤버 생일을 종합해서 반환한다")
    @Test
    fun getCoupleAnniversary() {
        // given
        val (userA, userB, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )
        userA.apply {
            birthDate = LocalDate.of(1990, 1, 10)
            nickname = "사용자A"
        }
        userB.apply {
            birthDate = LocalDate.of(1992, 2, 29)
            nickname = "사용자B"
        }
        userRepository.saveAll(listOf(userA, userB))

        val searchStart = LocalDate.of(2025, 1, 1)
        val searchEnd = LocalDate.of(2029, 1, 10)

        // when
        val result = coupleAnniversaryService.getCoupleAnniversary(
            startDate = searchStart,
            endDate = searchEnd,
            coupleId = couple.id,
            requestUserId = userA.id,
        )

        // then
        assertThat(result.coupleId).isEqualTo(couple.id)
        assertThat(result.startDate).isEqualTo(couple.startDate)
        assertThat(result.sharedMessage).isEqualTo(couple.sharedMessage)

        // 100일 단위 기념일
        assertThat(result.hundredDayAnniversaries.map { it.date to it.label })
            .containsExactly(
                couple.startDate!!.plusDays(99 + 0) to "100일",
                couple.startDate!!.plusDays(99 + 100) to "200일",
                couple.startDate!!.plusDays(99 + 200) to "300일",
            )
        assertThat(result.hundredDayAnniversaries.map { it.type }).allMatch { it == N_TH_DAY }

        // 주년 단위 기념일
        assertThat(result.yearlyAnniversaries.map { it.date to it.label to it.type })
            .containsExactly(
                couple.startDate!!.plusYears(1) to "1주년" to YEARLY,
                couple.startDate!!.plusYears(2) to "2주년" to YEARLY,
                couple.startDate!!.plusYears(3) to "3주년" to YEARLY,
                couple.startDate!!.plusYears(4) to "4주년" to YEARLY,
            )

        // 멤버들 생일
        val memberBirthDates = result.myBirthDates + result.partnerBirthDates
        assertThat(memberBirthDates.map { it.date to it.label to it.isAdjustedForNonLeapYear })
            .containsExactlyInAnyOrder(
                userA.birthDate!!.withYear(2025) to "사용자A 생일" to false,
                userA.birthDate!!.withYear(2026) to "사용자A 생일" to false,
                userA.birthDate!!.withYear(2027) to "사용자A 생일" to false,
                userA.birthDate!!.withYear(2028) to "사용자A 생일" to false,
                userA.birthDate!!.withYear(2029) to "사용자A 생일" to false,

                userB.birthDate!!.withYear(2025) to "사용자B 생일" to true,
                userB.birthDate!!.withYear(2026) to "사용자B 생일" to true,
                userB.birthDate!!.withYear(2027) to "사용자B 생일" to true,
                LocalDate.of(2028, 2, 29) to "사용자B 생일" to false,  // 윤년일 경우 그대로
            )
        assertThat(memberBirthDates.map { it.type }).allMatch { it == BIRTHDAY }
    }

    @DisplayName("조회 범위에 포함된 100일 기념일을 반환한다")
    @Test
    fun get100ThDay() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )
        val searchStart = couple.startDate!!
        val searchEnd = searchStart.minusDays(1).plusDays(300)

        // when
        val result = coupleAnniversaryService.get100ThDay(
            coupleStartDate = couple.startDate!!,
            startDate = searchStart,
            endDate = searchEnd
        )

        // then
        result.forEach { println(it) }
        assertThat(result.map { it.date to it.label })
            .containsExactly(
                couple.startDate!!.plusDays(99) to "100일",
                couple.startDate!!.plusDays(199) to "200일",
                couple.startDate!!.plusDays(299) to "300일",
            )

        assertThat(result.map { it.type }).allMatch { it == N_TH_DAY }
    }

    @DisplayName("조회 범위에 300일 이상의 100일 단위 기념일은 포함되지 않는다.")
    @Test
    fun get100ThDay_Over300ThAnniversary() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )
        val searchStart = couple.startDate!!.plusDays(300)
        val searchEnd = searchStart.plusMonths(1)  // DAYS.between=300 + 1 (301 > 300)

        // when
        val result = coupleAnniversaryService.get100ThDay(
            coupleStartDate = couple.startDate!!,
            startDate = searchStart,
            endDate = searchEnd,
        )

        // then
        assertThat(result).isEmpty()
    }

    @DisplayName("조회 범위에 포함된 100일 기념일이 없다면 빈 리스트를 반환한다.")
    @Test
    fun get100ThDay_WithoutAnniversary() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusDays(98)

        // when
        val result = coupleAnniversaryService.get100ThDay(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
        )

        // then
        assertThat(result).isEmpty()
    }

    @DisplayName("조회 범위에 포함된 n주년 기념일을 반환한다.")
    @Test
    fun getYearly() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusYears(2)

        // when
        val result = coupleAnniversaryService.getYearly(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
        )

        // then
        assertThat(result.map { it.date to it.label })
            .containsExactly(
                couple.startDate!!.plusYears(1) to "1주년",
                couple.startDate!!.plusYears(2) to "2주년",
            )

        assertThat(result.map { it.type }).allMatch { it == YEARLY }
    }

    @DisplayName("조회 범위에 포함된 n주년 기념일이 없다면 빈 리스트를 반환한다.")
    @Test
    fun getYearly_WithoutAnniversary() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2025, 1, 1)
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusYears(1).minusDays(1)

        // when
        val result = coupleAnniversaryService.getYearly(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
        )

        // then
        assertThat(result).isEmpty()
    }

    @DisplayName("윤년 2월 29일 시작일에 대해, 평년에는 2월 28일로 보정된 n주년 기념일이 반환된다.")
    @Test
    fun getYearly_WithAdjustedNonLeapYearFeb29() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2024, 2, 29)  // 윤년
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusYears(4)  // 다음 윤년까지 조회

        val adjustMonthDay = MonthDay.of(2, 28)

        // when
        val result = coupleAnniversaryService.getYearly(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
            feb29InNonLeapYearAdjust = adjustMonthDay
        )

        // then
        assertThat(result.map { it.date to it.label to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2025, 2, 28) to "1주년" to true,
                LocalDate.of(2026, 2, 28) to "2주년" to true,
                LocalDate.of(2027, 2, 28) to "3주년" to true,
                LocalDate.of(2028, 2, 29) to "4주년" to false,
            )
    }

    @DisplayName("윤년 2월 29일 시작일에 대해 잘못 보정했다면, 평년에는 2월 28일로 보정된 n주년 기념일이 반환된다")
    @Test
    fun getYearly_WithIllegalAdjustedNonLeapYearFeb29() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2024, 2, 29)
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusYears(4)

        // 윤년이 아닐 때 2월 29일은 존재하지 않으므로 잘못된 보정
        val illegalAdjustMonthDay = MonthDay.of(2, 29)

        // when
        val result = coupleAnniversaryService.getYearly(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
            feb29InNonLeapYearAdjust = illegalAdjustMonthDay
        )

        // then
        assertThat(result.map { it.date to it.label to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2025, 2, 28) to "1주년" to true,
                LocalDate.of(2026, 2, 28) to "2주년" to true,
                LocalDate.of(2027, 2, 28) to "3주년" to true,
                LocalDate.of(2028, 2, 29) to "4주년" to false,
            )
    }

    @DisplayName("윤년 2월 29일 시작일에 대해 보정을 적용하지 않았다면, 평년을 제외한 n주년 기념일이 반환된다")
    @Test
    fun getYearly_WithoutNonLeapYearFeb29() {
        // given
        val (_, _, couple) = makeCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            startDate = LocalDate.of(2024, 2, 29)
        )

        val searchStartDate = couple.startDate!!
        val searchEndDate = couple.startDate!!.plusYears(4)

        val adjustMonthDay = null

        // when
        val result = coupleAnniversaryService.getYearly(
            coupleStartDate = couple.startDate!!,
            startDate = searchStartDate,
            endDate = searchEndDate,
            feb29InNonLeapYearAdjust = adjustMonthDay,
        )

        // then
        assertThat(result.map { it.date to it.label to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2028, 2, 29) to "4주년" to false,
            )
    }

    @DisplayName("조회 범위에 포함된 생일을 반환한다")
    @Test
    fun getBirthDay_NormalDate() {
        // given
        val birthDate = LocalDate.of(1999, 10, 30)
        val searchStart = LocalDate.of(2025, 1, 1)
        val searchEnd = searchStart.plusYears(2)

        // when
        val birthDayLabel = "누구누구 생일"
        val result = coupleAnniversaryService.getBirthDay(
            ownerId = 0L,
            ownerNickname = "누구누구",
            userBirthDate = birthDate,
            startDate = searchStart,
            endDate = searchEnd,
            birthDayLabel = birthDayLabel,
        )

        // then
        assertThat(result.map { it.date })
            .containsExactly(
                LocalDate.of(2025, 10, 30),
                LocalDate.of(2026, 10, 30),
            )
        assertThat(result.map { it.label })
            .allMatch { it == birthDayLabel }
        assertThat(result.map { it.type })
            .allMatch { it == BIRTHDAY }
    }

    @DisplayName("2월 29일 생일은 평년엔 2월 28일로 보정하여 반환한다")
    @Test
    fun getBirthDay_Feb29Adjust() {
        // given
        val birthDate = LocalDate.of(2000, 2, 29)
        val searchStart = LocalDate.of(2025, 1, 1)
        val searchEnd = searchStart.plusYears(4)

        // when
        val birthDayLabel = "누구누구 생일"
        val result = coupleAnniversaryService.getBirthDay(
            ownerId = 0L,
            ownerNickname = "누구누구",
            userBirthDate = birthDate,
            startDate = searchStart,
            endDate = searchEnd,
            birthDayLabel = birthDayLabel,
        )

        // then
        assertThat(result.map { it.date to it.isAdjustedForNonLeapYear })
            .containsExactly(
                LocalDate.of(2025, 2, 28) to true,
                LocalDate.of(2026, 2, 28) to true,
                LocalDate.of(2027, 2, 28) to true,
                LocalDate.of(2028, 2, 29) to false
            )
        assertThat(result.map { it.label })
            .allMatch { it == birthDayLabel }
        assertThat(result.map { it.type })
            .allMatch { it == BIRTHDAY }
    }
}
