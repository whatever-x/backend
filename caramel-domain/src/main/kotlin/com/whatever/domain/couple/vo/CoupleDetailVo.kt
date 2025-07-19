package com.whatever.domain.couple.vo

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.model.CoupleStatus
import com.whatever.domain.user.model.User
import java.time.LocalDate

data class CoupleDetailVo(
    val coupleId: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
    val status: CoupleStatus,
    val myInfo: CoupleUserInfoVo,
    val partnerInfo: CoupleUserInfoVo,
) {
    companion object {
        fun from(
            couple: Couple,
            myUser: User,
            partnerUser: User,
        ): CoupleDetailVo {
            return CoupleDetailVo(
                coupleId = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                status = couple.status,
                myInfo = CoupleUserInfoVo.from(myUser),
                partnerInfo = CoupleUserInfoVo.from(partnerUser),
            )
        }
    }
} 
