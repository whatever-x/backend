package com.whatever.caramel.domain.notification.service.message

interface NotificationMessageParameter

data class YearlyAnniversaryParameter(
    val label: String
): NotificationMessageParameter

data class HundredAnniversaryParameter(
    val label: String
): NotificationMessageParameter

data class BirthDayParameter(
    val label: String,
    val birthdayMemberNickname: String,
    val isMyBirthday: Boolean,
): NotificationMessageParameter
