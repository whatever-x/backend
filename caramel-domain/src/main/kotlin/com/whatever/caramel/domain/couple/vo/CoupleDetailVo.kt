package com.whatever.caramel.domain.couple.vo

import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.model.CoupleStatus
import java.time.LocalDate

data class CoupleDetailVo(
    val id: Long,
    val startDate: LocalDate?,
    val sharedMessage: String?,
    val status: CoupleStatus,
    val myInfo: CoupleUserInfoVo,
    val partnerInfo: CoupleUserInfoVo,
) {
    companion object {
        fun from(
            couple: Couple,
            myUser: com.whatever.caramel.domain.user.model.User,
            partnerUser: com.whatever.caramel.domain.user.model.User,
        ): CoupleDetailVo {
            return CoupleDetailVo(
                id = couple.id,
                startDate = couple.startDate,
                sharedMessage = couple.sharedMessage,
                status = couple.status,
                myInfo = CoupleUserInfoVo.from(myUser),
                partnerInfo = CoupleUserInfoVo.from(partnerUser),
            )
        }
    }
} 
