package com.whatever.caramel.domain.couple.vo

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import java.time.LocalDate

sealed interface AnniversaryItem {
    val type: CoupleAnniversaryType
    val date: LocalDate
    val label: String
    val isAdjustedForNonLeapYear: Boolean
}

/**
 * 커플 공통 기념일
 */
data class CoupleAnniversaryItem(
    override val type: CoupleAnniversaryType,
    override val date: LocalDate,
    override val label: String,
    override val isAdjustedForNonLeapYear: Boolean = false
) : AnniversaryItem

/**
 * 소유자가 있는 커플 멤버 기념일
 */
data class MemberAnniversaryItem(
    val ownerId: Long,
    val ownerNickname: String,
    override val type: CoupleAnniversaryType,
    override val date: LocalDate,
    override val label: String,
    override val isAdjustedForNonLeapYear: Boolean = false
) : AnniversaryItem