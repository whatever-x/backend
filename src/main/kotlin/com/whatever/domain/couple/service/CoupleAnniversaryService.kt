package com.whatever.domain.couple.service

import com.whatever.domain.couple.controller.dto.response.CoupleAnniversaryDto
import com.whatever.domain.couple.controller.dto.response.CoupleAnniversaryResponse
import com.whatever.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.domain.couple.exception.CoupleNotFoundException
import com.whatever.domain.couple.model.CoupleAnniversaryType
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.global.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import com.whatever.util.AnniversaryUtil.findNThDayAnniversary
import com.whatever.util.AnniversaryUtil.findYearlyAnniversary
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.MonthDay

@Service
class CoupleAnniversaryService(
    private val coupleRepository: CoupleRepository,
) {

    fun getCoupleAnniversary(
        startDate: LocalDate,
        endDate: LocalDate,
        coupleId: Long = getCurrentUserCoupleId(),
        requestUserId: Long = getCurrentUserId(),
    ): CoupleAnniversaryResponse {
        val couple = coupleRepository.findByIdWithMembers(coupleId)
            ?: throw CoupleNotFoundException(COUPLE_NOT_FOUND)

        val hundredAnnivs = couple.startDate?.let { get100ThDay(it, startDate, endDate) }.orEmpty()
        val yearlyAnnivs = couple.startDate?.let { getYearly(it, startDate, endDate) }.orEmpty()

        val me = couple.members.firstOrNull { it.id == requestUserId }
        val myBirthDates = me?.birthDate?.let {
            getBirthDay(it, startDate, endDate, "${me.nickname!!} 생일")
        }.orEmpty()

        val partner = couple.members.firstOrNull { it.id != requestUserId }
        val partnerBirthDates = partner?.birthDate?.let {
            getBirthDay(it, startDate, endDate, "${partner.nickname!!} 생일")
        }.orEmpty()

        return CoupleAnniversaryResponse.of(
            couple = couple,
            hundredDayAnniversaries = hundredAnnivs,
            yearlyAnniversaries = yearlyAnnivs,
            myBirthDates = myBirthDates,
            partnerBirthDates = partnerBirthDates,
        )
    }

    fun get100ThDay(
        coupleStartDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        thDayLabel: String = "일",
    ): List<CoupleAnniversaryDto> {
        return findNThDayAnniversary(
            targetDate = coupleStartDate,
            startDate = startDate,
            endDate = endDate,
            interval = 100,
        ).mapNotNull {
            if (it.nTh > 300) {
                return@mapNotNull null
            }

            CoupleAnniversaryDto(
                type = CoupleAnniversaryType.N_TH_DAY,
                date = it.date,
                label = "${it.nTh}${thDayLabel}",
            )
        }
    }

    fun getYearly(
        coupleStartDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        yearlyLabel: String = "주년",
        feb29InNonLeapYearAdjust: MonthDay? = MonthDay.of(2, 28),
    ): List<CoupleAnniversaryDto> {
        return findYearlyAnniversary(
            targetDate = coupleStartDate,
            startDate = startDate,
            endDate = endDate,
            feb29InNonLeapYearAdjust = feb29InNonLeapYearAdjust
        ).map {
            CoupleAnniversaryDto(
                type = CoupleAnniversaryType.YEARLY,
                date = it.date,
                label = "${it.nTh}${yearlyLabel}",
                isAdjustedForNonLeapYear = it.isAdjustedForNonLeapYear
            )
        }
    }

    fun getBirthDay(
        userBirthDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        birthDayLabel: String = "생일",
        feb29InNonLeapYearAdjust: MonthDay? = MonthDay.of(2, 28),
    ): List<CoupleAnniversaryDto> {
        return findYearlyAnniversary(
            targetDate = userBirthDate,
            startDate = startDate,
            endDate = endDate,
            feb29InNonLeapYearAdjust = feb29InNonLeapYearAdjust
        ).map {
            CoupleAnniversaryDto(
                type = CoupleAnniversaryType.BIRTHDAY,
                date = it.date,
                label = birthDayLabel,
                isAdjustedForNonLeapYear = it.isAdjustedForNonLeapYear
            )
        }
    }
}

