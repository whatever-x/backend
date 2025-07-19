package com.whatever.domain.couple.vo

import com.whatever.domain.couple.model.CoupleAnniversaryType
import java.time.LocalDate

data class AnniversaryVo(
    val type: CoupleAnniversaryType,
    val date: LocalDate,
    val label: String,
    val isAdjustedForNonLeapYear: Boolean = false,
) {
    companion object {
        fun from(
            type: CoupleAnniversaryType,
            date: LocalDate,
            label: String,
            isAdjustedForNonLeapYear: Boolean = false,
        ): AnniversaryVo {
            return AnniversaryVo(
                type = type,
                date = date,
                label = label,
                isAdjustedForNonLeapYear = isAdjustedForNonLeapYear
            )
        }
    }
}
