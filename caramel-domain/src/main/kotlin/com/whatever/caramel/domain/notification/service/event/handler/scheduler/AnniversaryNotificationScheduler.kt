package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.domain.notification.service.message.BirthDayParameter
import com.whatever.caramel.domain.notification.service.message.HundredAnniversaryParameter
import com.whatever.caramel.domain.notification.service.message.NotificationMessageProvider
import com.whatever.caramel.domain.notification.service.message.YearlyAnniversaryParameter
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * CoupleAnniversaryType에 따른 알림 예약 생성을 담당하는 인터페이스
 */
interface AnniversaryNotificationScheduler {
    fun supports(): CoupleAnniversaryType
    fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter)
}

@Component
class HundredDayAnniversaryNotificationScheduler(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationMessageProvider: NotificationMessageProvider
) : AnniversaryNotificationScheduler {
    override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.N_TH_DAY
    override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
        val notificationType  = NotificationType.ANNIVERSARY_HUNDRED
        val param = notificationSchedulingParameter as? CoupleNotificationSchedulingParameter ?: throw IllegalArgumentException("")  // TODO Custom Exception

        val message = notificationMessageProvider.provide(
            type = notificationType,
            notificationMessageParameter = HundredAnniversaryParameter(label = param.anniversaryVo.label),
        )

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = param.memberIds.associateWith { memberId -> message },
            notificationType = notificationType,
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
    override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
        val notificationType  = NotificationType.ANNIVERSARY_YEARLY
        val param = notificationSchedulingParameter as? CoupleNotificationSchedulingParameter ?: throw IllegalArgumentException("")  // TODO Custom Exception

        val message = notificationMessageProvider.provide(
            type = notificationType,
            notificationMessageParameter = YearlyAnniversaryParameter(label = param.anniversaryVo.label),
        )

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = param.memberIds.associateWith { memberId -> message },
            notificationType = notificationType,
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
    override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
        val notificationType  = NotificationType.BIRTHDAY
        val param = notificationSchedulingParameter as? BirthDateNotificationSchedulingParameter ?: throw IllegalArgumentException("")  // TODO Custom Exception

        val messagesByUserId = setOf(param.birthdayMemberId, param.partnerId).associateWith { memberId ->
            notificationMessageProvider.provide(
                type = notificationType,
                notificationMessageParameter = BirthDayParameter(
                    label = param.anniversaryVo.label,
                    birthdayMemberNickname = param.birthdayMemberNickname,
                    isMyBirthday = memberId == param.birthdayMemberId,
                ),
            )
        }

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = messagesByUserId,
            notificationType = notificationType,
            notifyAt = notifyAt,
        )
    }
}
