package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.exception.InvalidMessageParameterException
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_HUNDRED
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_YEARLY
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {  }
/**
 * NotificationType에 따른 알림 메시지를 생성을 담당하는 인터페이스
 */
interface NotificationMessageGenerator {
    fun supports(): NotificationType
    fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo
}

abstract class AbstractNotificationMessageGenerator<T : NotificationMessageParameter>(
    private val expectedParameterType: KClass<T>,
) : NotificationMessageGenerator {
    final override fun generate(notificationMessageParameter: NotificationMessageParameter): NotificationMessageVo {
        if (!expectedParameterType.isInstance(notificationMessageParameter)) {
            logger.error {
                "Invalid parameter type for message generation. " +
                "Generator='${this::class.simpleName}', " +
                "Expected='${expectedParameterType.simpleName}', " +
                "Actual='${notificationMessageParameter::class.simpleName}', " +
                "ParameterData='${notificationMessageParameter}'"
            }
            throw InvalidMessageParameterException()
        }

        @Suppress("UNCHECKED_CAST")
        return generateMessage(notificationMessageParameter as T)
    }

    protected abstract fun generateMessage(parameter: T): NotificationMessageVo
}

@Component
class MyBirthdayMessageGenerator : AbstractNotificationMessageGenerator<BirthDayParameter>(BirthDayParameter::class) {
    override fun supports(): NotificationType = NotificationType.MY_BIRTHDAY
    override fun generateMessage(parameter: BirthDayParameter): NotificationMessageVo {
        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${parameter.label}일이에요!",
            body = "당신의 생일 축하축하",
        )
    }
}

@Component
class PartnerBirthdayMessageGenerator : AbstractNotificationMessageGenerator<BirthDayParameter>(BirthDayParameter::class) {
    override fun supports(): NotificationType = NotificationType.PARTNER_BIRTHDAY
    override fun generateMessage(parameter: BirthDayParameter): NotificationMessageVo {
        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${parameter.label}일이에요!",
            body = "${parameter.birthdayMemberNickname}님의 생일이니 축하해주시오",
        )
    }
}

@Component
class HundredAnniversaryMessageGenerator
    : AbstractNotificationMessageGenerator<HundredAnniversaryParameter>(HundredAnniversaryParameter::class) {
    override fun supports(): NotificationType = ANNIVERSARY_HUNDRED
    override fun generateMessage(parameter: HundredAnniversaryParameter): NotificationMessageVo {
        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${parameter.label}일이에요!",
            body = "해피해피 데이데이"
        )
    }
}

@Component
class YearlyAnniversaryMessageGenerator
    : AbstractNotificationMessageGenerator<YearlyAnniversaryParameter>(YearlyAnniversaryParameter::class) {
    override fun supports(): NotificationType = ANNIVERSARY_YEARLY
    override fun generateMessage(parameter: YearlyAnniversaryParameter): NotificationMessageVo {
        return NotificationMessageVo(
            type = supports(),
            title = "내일은 ${parameter.label} 기념일이에요!",
            body = "해피해피 데이데이"
        )
    }
}