package com.whatever.domain.couple.vo

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleStatus
import java.time.LocalDate

data class CoupleVo(
    val id: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
    val status: CoupleStatus,
) {
    companion object {
        fun from(couple: Couple): CoupleVo {
            return CoupleVo(
                id = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                status = couple.status,
            )
        }
    }
} 