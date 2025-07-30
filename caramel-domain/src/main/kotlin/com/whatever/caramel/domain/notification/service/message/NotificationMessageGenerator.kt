package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_HUNDRED
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_YEARLY
import com.whatever.caramel.domain.notification.vo.NotificationMessage
import org.springframework.stereotype.Component

interface NotificationMessageGenerator {
    fun supports(): NotificationType
    fun generate(info: NotificationInformation): NotificationMessage
}

@Component
class BirthdayMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = NotificationType.BIRTHDAY
    override fun generate(info: NotificationInformation): NotificationMessage {
        val birthdayInfo = info as? BirthDayInfo
            ?: throw IllegalArgumentException("Invalid parameter type for BIRTHDAY")  // TODO CustomException

        return NotificationMessage(
            title = "내일은 ${birthdayInfo.label}일이에요!",
            body = if (birthdayInfo.isMyBirthday) "당신의 생일 축하축하" else "${birthdayInfo.birthdayMemberNickname}님의 생일이니 축하해주시오"
        )
    }
}

@Component
class HundredAnniversaryMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = ANNIVERSARY_HUNDRED
    override fun generate(info: NotificationInformation): NotificationMessage {
        val anniversaryInfo = info as? HundredAnniversaryInfo
            ?: throw IllegalArgumentException("Invalid parameter type for ANNIVERSARY_HUNDRED")  // TODO CustomException

        return NotificationMessage(
            title = "내일은 ${anniversaryInfo.label}일이에요!",
            body = "해피해피 데이데이"
        )
    }
}

@Component
class YearlyAnniversaryMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = ANNIVERSARY_YEARLY
    override fun generate(info: NotificationInformation): NotificationMessage {
        val anniversaryInfo = info as? YearlyAnniversaryInfo
            ?: throw IllegalArgumentException("Invalid parameter type for ANNIVERSARY_YEARLY")  // TODO CustomException

        return NotificationMessage(
            title = "내일은 ${anniversaryInfo.label} 기념일이에요!",
            body = "해피해피 데이데이"
        )
    }
}