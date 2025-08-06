package com.whatever.caramel.domain.couple.vo

import com.whatever.caramel.domain.couple.model.Couple
import java.time.LocalDate

data class CoupleAnniversaryVo(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
    val hundredDayAnniversaries: List<AnniversaryItem>,
    val yearlyAnniversaries: List<AnniversaryItem>,
    val myBirthDates: List<AnniversaryItem>,
    val partnerBirthDates: List<AnniversaryItem>,
) {
    companion object {
        fun from(
            couple: Couple,
            hundredDayAnniversaries: List<AnniversaryItem>,
            yearlyAnniversaries: List<AnniversaryItem>,
            myBirthDates: List<AnniversaryItem>,
            partnerBirthDates: List<AnniversaryItem>,
        ): CoupleAnniversaryVo {
            return CoupleAnniversaryVo(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                hundredDayAnniversaries = hundredDayAnniversaries,
                yearlyAnniversaries = yearlyAnniversaries,
                myBirthDates = myBirthDates,
                partnerBirthDates = partnerBirthDates,
            )
        }
    }
}
