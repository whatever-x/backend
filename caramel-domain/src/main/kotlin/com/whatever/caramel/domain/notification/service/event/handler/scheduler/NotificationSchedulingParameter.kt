package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.vo.AnniversaryItem
import com.whatever.caramel.domain.couple.vo.CoupleAnniversaryItem
import com.whatever.caramel.domain.couple.vo.MemberAnniversaryItem

sealed interface NotificationSchedulingParameter {
    val anniversaryItem: AnniversaryItem
    val memberIds: Set<Long>
}

data class CoupleNotificationSchedulingParameter(
    override val anniversaryItem: CoupleAnniversaryItem,
    override val memberIds: Set<Long>,
) : NotificationSchedulingParameter

data class BirthDateNotificationSchedulingParameter(
    override val anniversaryItem: MemberAnniversaryItem,
    override val memberIds: Set<Long>,
    val birthdayMemberNickname: String,
    val birthdayMemberId: Long,
) : NotificationSchedulingParameter