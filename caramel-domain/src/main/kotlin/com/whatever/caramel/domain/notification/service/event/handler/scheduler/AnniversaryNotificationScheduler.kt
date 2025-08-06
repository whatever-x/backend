package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.notification.exception.InvalidSchedulingParameterException
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.domain.notification.service.message.BirthDayParameter
import com.whatever.caramel.domain.notification.service.message.HundredAnniversaryParameter
import com.whatever.caramel.domain.notification.service.message.NotificationMessageProvider
import com.whatever.caramel.domain.notification.service.message.YearlyAnniversaryParameter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {  }

/**
 * CoupleAnniversaryType에 따른 알림 예약 생성을 담당하는 인터페이스
 */
interface AnniversaryNotificationScheduler {
    fun supports(): CoupleAnniversaryType
    fun schedule(notifyAt: LocalDateTime, schedulingParameter: NotificationSchedulingParameter)
}

@Component
class HundredDayAnniversaryNotificationScheduler(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationMessageProvider: NotificationMessageProvider
) : AnniversaryNotificationScheduler {
    override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.N_TH_DAY
    override fun schedule(notifyAt: LocalDateTime, schedulingParameter: NotificationSchedulingParameter) {
        val notificationType  = NotificationType.ANNIVERSARY_HUNDRED

        if (schedulingParameter !is CoupleNotificationSchedulingParameter) {
            logger.error {
                "Invalid scheduling parameter type. " +
                "Expected: CoupleNotificationSchedulingParameter, " +
                "Actual: ${schedulingParameter::class.simpleName}, " +
                "parameter details: ${schedulingParameter}"
            }
            throw InvalidSchedulingParameterException()
        }


        val message = notificationMessageProvider.provide(
            type = notificationType,
            notificationMessageParameter = HundredAnniversaryParameter(label = schedulingParameter.anniversaryItem.label),
        )

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = schedulingParameter.memberIds.associateWith { memberId -> message },
            notifyAt = notifyAt,
        )
    }
}

@Component
class YearlyAnniversaryNotificationScheduler(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationMessageProvider: NotificationMessageProvider
) : AnniversaryNotificationScheduler {
    override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.YEARLY
    override fun schedule(notifyAt: LocalDateTime, schedulingParameter: NotificationSchedulingParameter) {
        val notificationType  = NotificationType.ANNIVERSARY_YEARLY
        if (schedulingParameter !is CoupleNotificationSchedulingParameter) {
            logger.error {
                "Invalid scheduling parameter type. " +
                "Expected: CoupleNotificationSchedulingParameter, " +
                "Actual: ${schedulingParameter::class.simpleName}, " +
                "parameter details: ${schedulingParameter}"
            }
            throw InvalidSchedulingParameterException()
        }

        val message = notificationMessageProvider.provide(
            type = notificationType,
            notificationMessageParameter = YearlyAnniversaryParameter(label = schedulingParameter.anniversaryItem.label),
        )

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = schedulingParameter.memberIds.associateWith { memberId -> message },
            notifyAt = notifyAt,
        )
    }
}

@Component
class BirthDateNotificationScheduler(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationMessageProvider: NotificationMessageProvider
) : AnniversaryNotificationScheduler {
    override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.BIRTHDAY
    override fun schedule(notifyAt: LocalDateTime, schedulingParameter: NotificationSchedulingParameter) {
        if (schedulingParameter !is BirthDateNotificationSchedulingParameter) {
            logger.error {
                "Invalid scheduling parameter type. " +
                "Expected: BirthDateNotificationSchedulingParameter, " +
                "Actual: ${schedulingParameter::class.simpleName}, " +
                "parameter details: ${schedulingParameter}"
            }
            throw InvalidSchedulingParameterException()
        }

        val messagesByUserId = schedulingParameter.memberIds.associateWith { memberId ->
            val notificationType =
                if (memberId == schedulingParameter.birthdayMemberId) NotificationType.MY_BIRTHDAY
                else NotificationType.PARTNER_BIRTHDAY

            notificationMessageProvider.provide(
                type = notificationType,
                notificationMessageParameter = BirthDayParameter(
                    label = schedulingParameter.anniversaryItem.label,
                    birthdayMemberNickname = schedulingParameter.birthdayMemberNickname,
                    isMyBirthday = memberId == schedulingParameter.birthdayMemberId,
                ),
            )
        }

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = messagesByUserId,
            notifyAt = notifyAt,
        )
    }
}