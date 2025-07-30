package com.whatever.caramel.domain.notification.service.event.handler

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.service.CoupleAnniversaryService
import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
import com.whatever.caramel.domain.couple.vo.AnniversaryVo
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_HUNDRED
import com.whatever.caramel.domain.notification.model.NotificationType.ANNIVERSARY_YEARLY
import com.whatever.caramel.domain.notification.model.NotificationType.BIRTHDAY
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.NotificationSchedulingParameter
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.BirthDateNotificationSchedulingParameter
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.CoupleNotificationSchedulingParameter
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.AnniversaryNotificationScheduler
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {  }

@Component
class AnniversaryUpdatedEventHandler(
    private val coupleAnniversaryService: CoupleAnniversaryService,
    private val scheduledNotificationService: ScheduledNotificationService,
    anniversaryNotificationSchedulers: List<AnniversaryNotificationScheduler>,
) {
    private val schedulerMap = anniversaryNotificationSchedulers.associateBy { it.supports() }

    @Transactional
    fun handle(event: CoupleStartDateUpdateEvent) {
        val today = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate()
        event.oldDate?.let { oldCoupleStartDate ->
            val anniversaryVos = getTodaysAnniversary(coupleStartDate = oldCoupleStartDate, today = today)
            deleteScheduledAnniversaryNotifications(
                anniversaryVos = anniversaryVos,
                memberIds = event.memberIds,
            )
        }

        val anniversaryVos = getTodaysAnniversary(coupleStartDate = event.newDate, today = today)
        scheduleAnniversaryNotification(
            anniversaryVos = anniversaryVos,
            memberIds = event.memberIds,
            today = today,
        )
    }

    private fun deleteScheduledAnniversaryNotifications(
        anniversaryVos: List<AnniversaryVo>,
        memberIds: Set<Long>,
    ) {
        anniversaryVos.forEach { anniversaryVo ->
            val typeToDelete = anniversaryVo.type.toNotificationType()
            val effectedRows = scheduledNotificationService.deleteScheduledNotifications(
                targetUserIds = memberIds,
                notificationTypes = setOf(typeToDelete),
            )
            logger.debug { "Deleted today's anniversary notifications ($typeToDelete) for users $memberIds. Effected rows: $effectedRows" }
        }
    }

    private fun scheduleAnniversaryNotification(
        anniversaryVos: List<AnniversaryVo>,
        memberIds: Set<Long>,
        today: LocalDate,
    ) {
        anniversaryVos.forEach { anniversaryVo ->
            val scheduler = schedulerMap[anniversaryVo.type] ?: run {
                logger.warn { "Unsupported couple anniversary type for notification scheduling: ${anniversaryVo.type}" }
                return@forEach  // continue to next anniversary
            }

            scheduler.schedule(
                notifyAt = today.toDateTime(),
                notificationSchedulingParameter = createAnniversarySchedulingParameter(anniversaryVo, memberIds),
            )
        }
    }

    private fun getTodaysAnniversary(
        coupleStartDate: LocalDate,
        today: LocalDate,
    ): List<AnniversaryVo> {
        val yearly = coupleAnniversaryService.getTodaysYearlyAnniversary(coupleStartDate, today)
        val hundredDay = coupleAnniversaryService.getTodaysHundredDaysAnniversary(coupleStartDate, today)

        return yearly + hundredDay
    }

    private fun createAnniversarySchedulingParameter(
        anniversaryVo: AnniversaryVo,
        memberIds: Set<Long>
    ): NotificationSchedulingParameter {
        return when (anniversaryVo.type) {
            CoupleAnniversaryType.N_TH_DAY, CoupleAnniversaryType.YEARLY -> {
                CoupleNotificationSchedulingParameter(anniversaryVo = anniversaryVo, memberIds = memberIds)
            }

            CoupleAnniversaryType.BIRTHDAY -> {
                BirthDateNotificationSchedulingParameter(
                    anniversaryVo = anniversaryVo,
                    birthdayMemberId = requireNotNull(anniversaryVo.ownerId),
                    birthdayMemberNickname = requireNotNull(anniversaryVo.ownerNickname),
                    partnerId = memberIds.first { it != anniversaryVo.ownerId }
                )
            }
        }
    }
}

private fun CoupleAnniversaryType.toNotificationType(): NotificationType {
    return when (this) {
        CoupleAnniversaryType.N_TH_DAY -> ANNIVERSARY_HUNDRED
        CoupleAnniversaryType.YEARLY -> ANNIVERSARY_YEARLY
        CoupleAnniversaryType.BIRTHDAY -> BIRTHDAY
    }
}
private fun CoupleAnniversaryService.getTodaysYearlyAnniversary(
    coupleStartDate: LocalDate,
    today: LocalDate,
): List<AnniversaryVo> {
    return getYearly(
        coupleStartDate = coupleStartDate,
        startDate = today,
        endDate = today,
    )
}
private fun CoupleAnniversaryService.getTodaysHundredDaysAnniversary(
    coupleStartDate: LocalDate,
    today: LocalDate,
): List<AnniversaryVo> {
    return get100ThDay(
        coupleStartDate = coupleStartDate,
        startDate = today,
        endDate = today,
    )
}