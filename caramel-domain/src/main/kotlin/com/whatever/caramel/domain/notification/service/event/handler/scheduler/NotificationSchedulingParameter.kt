package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.vo.AnniversaryVo

sealed interface NotificationSchedulingParameter {
    val anniversaryVo: AnniversaryVo
    val memberIds: Set<Long>
}

data class CoupleNotificationSchedulingParameter(
    override val anniversaryVo: AnniversaryVo,
    override val memberIds: Set<Long>,
) : NotificationSchedulingParameter

data class BirthDateNotificationSchedulingParameter(
    override val anniversaryVo: AnniversaryVo,
    override val memberIds: Set<Long>,
    val birthdayMemberNickname: String,
    val birthdayMemberId: Long,
) : NotificationSchedulingParameter
