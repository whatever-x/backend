package com.whatever.caramel.domain.couple.vo

import com.whatever.caramel.domain.couple.model.Couple
import java.time.LocalDate

data class CoupleAnniversaryVo(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
    val hundredDayAnniversaries: List<AnniversaryVo>,
    val yearlyAnniversaries: List<AnniversaryVo>,
    val myBirthDates: List<AnniversaryVo>,
    val partnerBirthDates: List<AnniversaryVo>,
) {
    companion object {
        fun from(
            couple: Couple,
            hundredDayAnniversaries: List<AnniversaryVo>,
            yearlyAnniversaries: List<AnniversaryVo>,
            myBirthDates: List<AnniversaryVo>,
            partnerBirthDates: List<AnniversaryVo>,
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
