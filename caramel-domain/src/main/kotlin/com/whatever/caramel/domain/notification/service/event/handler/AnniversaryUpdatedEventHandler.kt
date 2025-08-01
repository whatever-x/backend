package com.whatever.caramel.domain.notification.service.event.handler

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.service.CoupleAnniversaryService
import com.whatever.caramel.domain.couple.service.CoupleService
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
import com.whatever.caramel.domain.user.service.event.dto.UserBirthDateUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val logger = KotlinLogging.logger {  }

@Component
class AnniversaryUpdatedEventHandler(
    anniversaryNotificationSchedulers: List<AnniversaryNotificationScheduler>,
    private val scheduledNotificationService: ScheduledNotificationService,
    private val coupleAnniversaryService: CoupleAnniversaryService,
    private val coupleService: CoupleService,
) {
    private val schedulerMap = anniversaryNotificationSchedulers.associateBy { it.supports() }

    @Transactional
    fun handle(
        event: CoupleStartDateUpdateEvent,
        targetDate: LocalDate = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate(),
    ) {
        event.oldDate?.let { oldCoupleStartDate ->
            val anniversaryVos = findAnniversariesOn(coupleStartDate = oldCoupleStartDate, targetDate = targetDate)
            deleteScheduledAnniversaryNotifications(
                anniversaryVos = anniversaryVos,
                memberIds = event.memberIds,
            )
        }

        val anniversaryVos = findAnniversariesOn(coupleStartDate = event.newDate, targetDate = targetDate)
        scheduleAnniversaryNotification(
            anniversaryVos = anniversaryVos,
            memberIds = event.memberIds,
            targetDate = targetDate,
        )
    }

    @Transactional
    fun handle(
        event: UserBirthDateUpdateEvent,
        targetDate: LocalDate = DateTimeUtil.localNow(KST_ZONE_ID).toLocalDate(),
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
                anniversaryVos = birthDate,
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
            anniversaryVos = birthDate,
            memberIds = memberIds,
            targetDate = targetDate,
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
        targetDate: LocalDate,
    ) {
        anniversaryVos.forEach { anniversaryVo ->
            val scheduler = schedulerMap[anniversaryVo.type] ?: run {
                logger.warn { "Unsupported couple anniversary type for notification scheduling: ${anniversaryVo.type}" }
                return@forEach  // continue to next anniversary
            }

            scheduler.schedule(
                notifyAt = targetDate.toDateTime(),
                notificationSchedulingParameter = createAnniversarySchedulingParameter(anniversaryVo, memberIds),
            )
        }
    }

    private fun findAnniversariesOn(
        coupleStartDate: LocalDate,
        targetDate: LocalDate,
    ): List<AnniversaryVo> {
        val yearly = coupleAnniversaryService.findYearlyAnniversaryOn(coupleStartDate, targetDate)
        val hundredDay = coupleAnniversaryService.findHundredDaysAnniversaryOn(coupleStartDate, targetDate)

        return yearly + hundredDay
    }

    private fun findBirthDateOn(
        ownerId: Long,
        ownerNickname: String,
        birthDate: LocalDate,
        targetDate: LocalDate,
    ): List<AnniversaryVo> {
        return coupleAnniversaryService.getBirthDay(
            ownerId = ownerId,
            ownerNickname = ownerNickname,
            userBirthDate = birthDate,
            startDate = targetDate,
            endDate = targetDate,
        )
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
private fun CoupleAnniversaryService.findYearlyAnniversaryOn(
    coupleStartDate: LocalDate,
    targetDate: LocalDate,
): List<AnniversaryVo> {
    return getYearly(
        coupleStartDate = coupleStartDate,
        startDate = targetDate,
        endDate = targetDate,
    )
}
private fun CoupleAnniversaryService.findHundredDaysAnniversaryOn(
    coupleStartDate: LocalDate,
    targetDate: LocalDate,
): List<AnniversaryVo> {
    return get100ThDay(
        coupleStartDate = coupleStartDate,
        startDate = targetDate,
        endDate = targetDate,
    )
}
