package com.whatever.domain.couple.vo

import java.time.LocalDateTime

data class CoupleInvitationCodeVo(
    val invitationCode: String,
    val expirationDateTime: LocalDateTime?,
) 