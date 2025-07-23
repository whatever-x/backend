package com.whatever.caramel.domain.couple.service

import com.whatever.caramel.common.util.AnniversaryUtil.findNThDayAnniversary
import com.whatever.caramel.common.util.AnniversaryUtil.findYearlyAnniversary
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.COUPLE_NOT_FOUND
import com.whatever.caramel.domain.couple.exception.CoupleNotFoundException
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.vo.AnniversaryVo
import com.whatever.caramel.domain.couple.vo.CoupleAnniversaryVo
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
        coupleId: Long,
        requestUserId: Long,
    ): CoupleAnniversaryVo {
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

        return CoupleAnniversaryVo.from(
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
    ): List<AnniversaryVo> {
        return findNThDayAnniversary(
            targetDate = coupleStartDate,
            startDate = startDate,
            endDate = endDate,
            interval = 100,
        ).mapNotNull {
            if (it.nTh > 300) {
                return@mapNotNull null
            }

            AnniversaryVo.from(
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
    ): List<AnniversaryVo> {
        return findYearlyAnniversary(
            targetDate = coupleStartDate,
            startDate = startDate,
            endDate = endDate,
            feb29InNonLeapYearAdjust = feb29InNonLeapYearAdjust
        ).map {
            AnniversaryVo.from(
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
    ): List<AnniversaryVo> {
        return findYearlyAnniversary(
            targetDate = userBirthDate,
            startDate = startDate,
            endDate = endDate,
            feb29InNonLeapYearAdjust = feb29InNonLeapYearAdjust
        ).map {
            AnniversaryVo.from(
                type = CoupleAnniversaryType.BIRTHDAY,
                date = it.date,
                label = birthDayLabel,
                isAdjustedForNonLeapYear = it.isAdjustedForNonLeapYear
            )
        }
    }
}

