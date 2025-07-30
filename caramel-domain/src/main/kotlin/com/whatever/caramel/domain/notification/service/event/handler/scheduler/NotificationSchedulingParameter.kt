package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.vo.AnniversaryVo

interface NotificationSchedulingParameter

data class CoupleNotificationSchedulingParameter(
    val anniversaryVo: AnniversaryVo,
    val memberIds: Set<Long>,
) : NotificationSchedulingParameter

data class BirthDateNotificationSchedulingParameter(
    val anniversaryVo: AnniversaryVo,
    val birthdayMemberNickname: String,
    val birthdayMemberId: Long,
    val partnerId: Long,
) : NotificationSchedulingParameter