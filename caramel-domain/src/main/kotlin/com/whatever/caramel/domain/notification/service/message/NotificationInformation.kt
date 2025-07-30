package com.whatever.caramel.domain.notification.service.message

interface NotificationInformation

data class YearlyAnniversaryInfo(
    val label: String
): NotificationInformation

data class HundredAnniversaryInfo(
    val label: String
): NotificationInformation

data class BirthDayInfo(
    val label: String,
    val birthdayMemberNickname: String,
    val isMyBirthday: Boolean,
): NotificationInformation
