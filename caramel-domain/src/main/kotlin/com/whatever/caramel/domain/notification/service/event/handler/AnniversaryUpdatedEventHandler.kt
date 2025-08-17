package com.whatever.caramel.domain.notification.service.event.handler

import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.service.CoupleAnniversaryService
import com.whatever.caramel.domain.couple.service.CoupleService
import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
import com.whatever.caramel.domain.couple.vo.AnniversaryItem
import com.whatever.caramel.domain.couple.vo.CoupleAnniversaryItem
import com.whatever.caramel.domain.couple.vo.MemberAnniversaryItem
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_HUNDRED
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_YEARLY
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.AnniversaryNotificationSchedulerProvider
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.BirthDateNotificationSchedulingParameter
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.CoupleNotificationSchedulingParameter
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.NotificationSchedulingParameter
import com.whatever.caramel.domain.user.service.event.dto.UserBirthDateUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {  }

@Component
class AnniversaryUpdatedEventHandler(
    private val schedulerProvider: AnniversaryNotificationSchedulerProvider,
    private val scheduledNotificationService: ScheduledNotificationService,
    private val coupleAnniversaryService: CoupleAnniversaryService,
    private val coupleService: CoupleService,
) {
    @Transactional
    fun handle(
        event: CoupleStartDateUpdateEvent,
        targetDate: LocalDate,
    ) {
        event.oldDate?.let { oldCoupleStartDate ->
            val anniversaryVos = findAnniversariesOn(coupleStartDate = oldCoupleStartDate, targetDate = targetDate)
            deleteScheduledAnniversaryNotifications(
                anniversaryItems = anniversaryVos,
                memberIds = event.memberIds,
            )
        }

        val anniversaryVos = findAnniversariesOn(coupleStartDate = event.newDate, targetDate = targetDate)
        scheduleAnniversaryNotification(
            anniversaryItems = anniversaryVos,
            memberIds = event.memberIds,
            targetDate = targetDate,
        )
    }

    @Transactional
    fun handle(
        event: UserBirthDateUpdateEvent,
        targetDate: LocalDate,
    ) {
        val memberIds = coupleService.getCoupleAndMemberInfo(event.coupleId, event.userId)
            .run { setOf(myInfo.id, partnerInfo.id) }

        event.oldDate?.let { oldBirthDate ->
            val birthDate = findBirthDateOn(
                ownerId = event.userId,
                ownerNickname = event.userNickname,
                birthDate = oldBirthDate,
                targetDate = targetDate,
            )
            deleteScheduledAnniversaryNotifications(
                anniversaryItems = birthDate,
                memberIds = memberIds,
            )
        }

        val birthDate = findBirthDateOn(
            ownerId = event.userId,
            ownerNickname = event.userNickname,
            birthDate = event.newDate,
            targetDate = targetDate,
        )
        scheduleAnniversaryNotification(
            anniversaryItems = birthDate,
            memberIds = memberIds,
            targetDate = targetDate,
        )
    }

private fun deleteScheduledAnniversaryNotifications(
    anniversaryItems: List<AnniversaryItem>,
    memberIds: Set<Long>,
) {
    anniversaryItems.forEach { anniversaryVo ->
        val typeToDelete = anniversaryVo.type.toNotificationType()

        when (anniversaryVo) {
            is CoupleAnniversaryItem -> {
                val effectedRows = scheduledNotificationService.deleteScheduledNotifications(
                    targetUserIds = memberIds,
                    notificationTypes = typeToDelete,
                )
                logger.debug { "Deleted today's anniversary notifications ($typeToDelete) for users $memberIds. Effected rows: $effectedRows" }
            }

            is MemberAnniversaryItem -> {
                val ownerId = anniversaryVo.ownerId
                val deletionMemberIdByNotificationType = memberIds.groupBy { memberId ->
                    if (memberId == ownerId) NotificationType.MY_BIRTHDAY
                    else NotificationType.PARTNER_BIRTHDAY
                }

                deletionMemberIdByNotificationType.forEach { (notificationType, memberIds) ->
                    scheduledNotificationService.deleteScheduledNotifications(
                        targetUserIds = memberIds.toSet(),
                        notificationTypes = setOf(notificationType),
                    )
                }
            }
        }
    }
}

    private fun scheduleAnniversaryNotification(
        anniversaryItems: List<AnniversaryItem>,
        memberIds: Set<Long>,
        targetDate: LocalDate,
    ) {
        anniversaryItems.forEach { anniversary ->
            schedulerProvider.provide(anniversary.type).schedule(
                notifyAt = targetDate.toDateTime(),
                schedulingParameter = createAnniversarySchedulingParameter(anniversary, memberIds),
            )
        }
    }

    private fun findAnniversariesOn(
        coupleStartDate: LocalDate,
        targetDate: LocalDate,
    ): List<AnniversaryItem> {
        val yearly = coupleAnniversaryService.findYearlyAnniversaryOn(coupleStartDate, targetDate)
        val hundredDay = coupleAnniversaryService.findHundredDaysAnniversaryOn(coupleStartDate, targetDate)

        return yearly + hundredDay
    }

    private fun findBirthDateOn(
        ownerId: Long,
        ownerNickname: String,
        birthDate: LocalDate,
        targetDate: LocalDate,
    ): List<AnniversaryItem> {
        return coupleAnniversaryService.getBirthDay(
            ownerId = ownerId,
            ownerNickname = ownerNickname,
            userBirthDate = birthDate,
            startDate = targetDate,
            endDate = targetDate,
        )
    }

    private fun createAnniversarySchedulingParameter(
        anniversaryItem: AnniversaryItem,
        memberIds: Set<Long>
    ): NotificationSchedulingParameter {
        return when (anniversaryItem) {
             is CoupleAnniversaryItem -> {
                CoupleNotificationSchedulingParameter(anniversaryItem = anniversaryItem, memberIds = memberIds)
            }
            is MemberAnniversaryItem -> {
                BirthDateNotificationSchedulingParameter(anniversaryItem = anniversaryItem, memberIds = memberIds)
            }
        }
    }
}

private fun CoupleAnniversaryType.toNotificationType(): Set<NotificationType> {
    return when (this) {
        CoupleAnniversaryType.N_TH_DAY -> setOf(ANNIVERSARY_HUNDRED)
        CoupleAnniversaryType.YEARLY -> setOf(ANNIVERSARY_YEARLY)
        CoupleAnniversaryType.BIRTHDAY -> setOf(NotificationType.MY_BIRTHDAY, NotificationType.PARTNER_BIRTHDAY)
    }
}
private fun CoupleAnniversaryService.findYearlyAnniversaryOn(
    coupleStartDate: LocalDate,
    targetDate: LocalDate,
): List<AnniversaryItem> {
    return getYearly(
        coupleStartDate = coupleStartDate,
        startDate = targetDate,
        endDate = targetDate,
    )
}
private fun CoupleAnniversaryService.findHundredDaysAnniversaryOn(
    coupleStartDate: LocalDate,
    targetDate: LocalDate,
): List<AnniversaryItem> {
    return get100ThDay(
        coupleStartDate = coupleStartDate,
        startDate = targetDate,
        endDate = targetDate,
    )
}
