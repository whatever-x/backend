package com.whatever.caramel.domain.notification.service.message

/**
 * Notificatio의 Message를 생성에 필요한 정보를 담은 파라미터
 */
sealed interface NotificationMessageParameter {
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
