package com.whatever.caramel.domain.couple.vo

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import java.time.LocalDate

data class AnniversaryVo(
    val ownerId: Long? = null,
    val type: CoupleAnniversaryType,
    val date: LocalDate,
    val label: String,
    val isAdjustedForNonLeapYear: Boolean = false,
) {
    companion object {
        fun from(
            ownerId: Long? = null,
            type: CoupleAnniversaryType,
            date: LocalDate,
            label: String,
            isAdjustedForNonLeapYear: Boolean = false,
        ): AnniversaryVo {
            return AnniversaryVo(
                ownerId = ownerId,
                type = type,
                date = date,
                label = label,
                isAdjustedForNonLeapYear = isAdjustedForNonLeapYear
            )
        }
    }
}
