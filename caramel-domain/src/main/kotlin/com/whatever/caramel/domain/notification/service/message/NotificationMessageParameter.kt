package com.whatever.caramel.domain.notification.service.message

interface NotificationMessageParameter {
    val label: String
}

data class YearlyAnniversaryParameter(
    override val label: String
): NotificationMessageParameter

data class HundredAnniversaryParameter(
    override val label: String
): NotificationMessageParameter

data class BirthDayParameter(
    override val label: String,
    val birthdayMemberNickname: String,
    val isMyBirthday: Boolean,
): NotificationMessageParameter
