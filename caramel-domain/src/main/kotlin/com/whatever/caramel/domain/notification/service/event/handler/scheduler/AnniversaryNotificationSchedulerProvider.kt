package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.notification.exception.UnsupportedCoupleAnniversaryTypeException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }
@Component
class AnniversaryNotificationSchedulerProvider(
    schedulers: List<AnniversaryNotificationScheduler>,
) {
    private val schedulerMap = schedulers.associateBy { it.supports() }

    fun provide(anniversaryType: CoupleAnniversaryType): AnniversaryNotificationScheduler {
        return schedulerMap.getOrElse(anniversaryType) {
            logger.warn { "Unsupported couple anniversary type for notification scheduling: ${anniversaryType}" }
            throw UnsupportedCoupleAnniversaryTypeException(detailMessage = "No notification scheduler found for CoupleAnniversaryType: ${anniversaryType}")
        }
    }
}