package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.exception.InvalidMessageParameterException
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_HUNDRED
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_YEARLY
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }
/**
 * NotificationType에 따른 알림 메시지를 생성을 담당하는 인터페이스
 */
interface NotificationMessageGenerator {
    fun supports(): NotificationType
    fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo
}

@Component
class MyBirthdayMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = NotificationType.MY_BIRTHDAY
    override fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo {
        if (notificationMessageParameter !is BirthDayParameter) {
            logger.error {
                "Invalid parameter type for MyBirthdayMessageGenerator. " +
                "Expected: BirthDayParameter, " +
                "Actual: ${notificationMessageParameter::class.simpleName}, " +
                "parameter details: $notificationMessageParameter"
            }
            throw InvalidMessageParameterException()
        }

        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${notificationMessageParameter.label}일이에요!",
            body = "당신의 생일 축하축하",
        )
    }
}

@Component
class PartnerBirthdayMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = NotificationType.PARTNER_BIRTHDAY
    override fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo {
        if (notificationMessageParameter !is BirthDayParameter) {
            logger.error {
                "Invalid parameter type for MyBirthdayMessageGenerator. " +
                "Expected: BirthDayParameter, " +
                "Actual: ${notificationMessageParameter::class.simpleName}, " +
                "parameter details: $notificationMessageParameter"
            }
            throw InvalidMessageParameterException()
        }

        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${notificationMessageParameter.label}일이에요!",
            body = "${notificationMessageParameter.birthdayMemberNickname}님의 생일이니 축하해주시오",
        )
    }
}

@Component
class HundredAnniversaryMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = ANNIVERSARY_HUNDRED
    override fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo {
        if (notificationMessageParameter !is HundredAnniversaryParameter) {
            logger.error {
                "Invalid parameter type for HundredAnniversaryMessageGenerator. " +
                "Expected: HundredAnniversaryParameter, " +
                "Actual: ${notificationMessageParameter::class.simpleName}, " +
                "parameter details: $notificationMessageParameter"
            }
            throw InvalidMessageParameterException()
        }

        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${notificationMessageParameter.label}일이에요!",
            body = "해피해피 데이데이"
        )
    }
}

@Component
class YearlyAnniversaryMessageGenerator : NotificationMessageGenerator {
    override fun supports(): NotificationType = ANNIVERSARY_YEARLY
    override fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo {
        if (notificationMessageParameter !is YearlyAnniversaryParameter) {
            logger.error {
                "Invalid parameter type for YearlyAnniversaryMessageGenerator. " +
                "Expected: YearlyAnniversaryParameter, " +
                "Actual: ${notificationMessageParameter::class.simpleName}, " +
                "parameter details: $notificationMessageParameter"
            }
            throw InvalidMessageParameterException()
        }

        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${notificationMessageParameter.label} 기념일이에요!",
            body = "해피해피 데이데이"
        )
    }
}