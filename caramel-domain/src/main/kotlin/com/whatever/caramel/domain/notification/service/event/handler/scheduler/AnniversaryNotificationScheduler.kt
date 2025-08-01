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

interface AnniversaryNotificationSchedulerV2 {
    fun schedule(
        notifyAt: LocalDateTime,
        anniversaryType: CoupleAnniversaryType,
        notificationSchedulingParameter: NotificationSchedulingParameter,
    )
}

@Component
class AnniversaryNotificationSchedulerImpl(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val notificationMessageProvider: NotificationMessageProvider,
) : AnniversaryNotificationSchedulerV2 {

    override fun schedule(
        notifyAt: LocalDateTime,
        anniversaryType: CoupleAnniversaryType,
        notificationSchedulingParameter: NotificationSchedulingParameter,
    ) {
        val notificationType = when (anniversaryType) {
            CoupleAnniversaryType.N_TH_DAY -> NotificationType.ANNIVERSARY_HUNDRED
            CoupleAnniversaryType.YEARLY -> NotificationType.ANNIVERSARY_YEARLY
            CoupleAnniversaryType.BIRTHDAY -> NotificationType.BIRTHDAY
        }

        val messagesByUserId = notificationSchedulingParameter.memberIds.associateWith { memberId ->
            val parameter = when (notificationSchedulingParameter) {
                is BirthDateNotificationSchedulingParameter -> {
                    when (notificationType) {
                        NotificationType.BIRTHDAY -> BirthDayParameter(
                            label = notificationSchedulingParameter.anniversaryVo.label,
                            birthdayMemberNickname = notificationSchedulingParameter.birthdayMemberNickname,
                            isMyBirthday = notificationSchedulingParameter.birthdayMemberId == memberId,
                        )

                        else -> {
                            error("헉")
                        }
                    }
                }

                is CoupleNotificationSchedulingParameter -> {
                    when (notificationType) {
                        NotificationType.ANNIVERSARY_HUNDRED -> {
                            HundredAnniversaryParameter(label = notificationSchedulingParameter.anniversaryVo.label)
                        }

                        NotificationType.ANNIVERSARY_YEARLY -> {
                            YearlyAnniversaryParameter(label = notificationSchedulingParameter.anniversaryVo.label)
                        }

                        else -> {
                            error("헉")
                        }
                    }
                }
            }
            notificationMessageProvider.provide(
                type = notificationType,
                notificationMessageParameter = parameter
            )
        }

        scheduledNotificationService.scheduleNotifications(
            messagesByUserId = messagesByUserId,
            notificationType = notificationType,
            notifyAt = notifyAt,
        )
    }
}

// @Component
// class HundredDayAnniversaryNotificationScheduler(
//     private val scheduledNotificationService: ScheduledNotificationService,
//     private val notificationMessageProvider: NotificationMessageProvider,
// ) : AnniversaryNotificationScheduler {
//     override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.N_TH_DAY
//     override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
//         val notificationType = NotificationType.ANNIVERSARY_HUNDRED
//         val param =
//             notificationSchedulingParameter as? CoupleNotificationSchedulingParameter ?: throw IllegalArgumentException(
//                 ""
//             )  // TODO Custom Exception
//
//         val message = notificationMessageProvider.provide(
//             type = notificationType,
//             notificationMessageParameter = HundredAnniversaryParameter(label = param.anniversaryVo.label),
//         )
//
//         scheduledNotificationService.scheduleNotifications(
//             messagesByUserId = param.memberIds.associateWith { memberId -> message },
//             notificationType = notificationType,
//             notifyAt = notifyAt,
//         )
//     }
// }
//
// @Component
// class YearlyAnniversaryNotificationScheduler(
//     private val scheduledNotificationService: ScheduledNotificationService,
//     private val notificationMessageProvider: NotificationMessageProvider,
// ) : AnniversaryNotificationScheduler {
//     override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.YEARLY
//     override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
//         val notificationType = NotificationType.ANNIVERSARY_YEARLY
//         val param =
//             notificationSchedulingParameter as? CoupleNotificationSchedulingParameter ?: throw IllegalArgumentException(
//                 ""
//             )  // TODO Custom Exception
//
//         val message = notificationMessageProvider.provide(
//             type = notificationType,
//             notificationMessageParameter = YearlyAnniversaryParameter(label = param.anniversaryVo.label),
//         )
//
//         scheduledNotificationService.scheduleNotifications(
//             messagesByUserId = param.memberIds.associateWith { memberId -> message },
//             notificationType = notificationType,
//             notifyAt = notifyAt,
//         )
//     }
// }
//
// @Component
// class BirthDateNotificationScheduler(
//     private val scheduledNotificationService: ScheduledNotificationService,
//     private val notificationMessageProvider: NotificationMessageProvider,
// ) : AnniversaryNotificationScheduler {
//     override fun supports(): CoupleAnniversaryType = CoupleAnniversaryType.BIRTHDAY
//     override fun schedule(notifyAt: LocalDateTime, notificationSchedulingParameter: NotificationSchedulingParameter) {
//         val notificationType = NotificationType.BIRTHDAY
//         val param = notificationSchedulingParameter as? BirthDateNotificationSchedulingParameter
//             ?: throw IllegalArgumentException("")  // TODO Custom Exception
//
//         // 메시지 2개 만듦
//         val messagesByUserId = setOf(param.memberIds.first(), param.memberIds.first()).associateWith { memberId ->
//             notificationMessageProvider.provide(
//                 type = notificationType,
//                 notificationMessageParameter = BirthDayParameter(
//                     label = param.anniversaryVo.label,
//                     birthdayMemberNickname = param.birthdayMemberNickname,
//                     isMyBirthday = memberId == param.memberIds.first(),
//                 ),
//             )
//         }
//
//         scheduledNotificationService.scheduleNotifications(
//             messagesByUserId = messagesByUserId,
//             notificationType = notificationType,
//             notifyAt = notifyAt,
//         )
//     }
// }
