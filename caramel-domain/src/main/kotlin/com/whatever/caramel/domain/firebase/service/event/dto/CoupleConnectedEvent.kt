package com.whatever.caramel.domain.firebase.service.event.dto

import com.whatever.caramel.domain.couple.vo.CoupleDetailVo
import com.whatever.caramel.domain.couple.vo.CoupleUserInfoVo

data class CoupleConnectedEvent(
    val coupleDetailVo: CoupleDetailVo,
) {
    val memberIds : Set<Long>
        get() {
            return setOf(
                coupleDetailVo.myInfo.id,
                coupleDetailVo.partnerInfo.id,
            )
        }
    val members : List<CoupleUserInfoVo>
        get() {
            return listOf(
                coupleDetailVo.myInfo,
                coupleDetailVo.partnerInfo,
            )
        }
}
