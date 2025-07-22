package com.whatever.domain.firebase.service.event

import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import com.whatever.domain.firebase.service.event.dto.CoupleConnectedEvent
import com.whatever.domain.firebase.service.event.dto.MemoCreateEvent
import com.whatever.domain.firebase.service.event.dto.ScheduleCreateEvent
import com.whatever.domain.firebase.service.FirebaseService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger { }

@Component
class FcmEventListener(
    private val firebaseService: FirebaseService,
) {

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun memoCreated(event: MemoCreateEvent) {
        val partnerUserId = runCatching { event.memberIds.first { it != event.userId } }
            .onFailure { e ->
                logger.warn { "FCM: partner userId not found in ${event.memberIds} for initiator ${event.userId}" }
            }.getOrElse { return }

        val fcmNotification = FcmNotification(
            title = "메모 등록",
            body = "연인이 새로운 메모를 등록했어요!",
        )

        firebaseService.sendNotification(
            targetUserIds = setOf(partnerUserId),
            fcmNotification = fcmNotification,
        )
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun scheduleCreated(event: ScheduleCreateEvent) {
        val partnerUserId = runCatching { event.memberIds.first { it != event.userId } }
            .onFailure { e ->
                logger.warn { "FCM: partner userId not found in ${event.memberIds} for initiator ${event.userId}" }
            }.getOrElse { return }

        val fcmNotification = FcmNotification(
            title = "일정 등록",
            body = "연인이 새로운 일정을 등록했어요!",
        )

        firebaseService.sendNotification(
            targetUserIds = setOf(partnerUserId),
            fcmNotification = fcmNotification,
        )
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Async
    fun coupleConnected(event: CoupleConnectedEvent) {
        val fcmNotification = FcmNotification(
            title = "커플 연결 성공",
            body = "커플 연결에 성공했어요! 둘만의 공간에서 커플 일정과 메모를 관리해 보세요.",
        )

        firebaseService.sendNotification(
            targetUserIds = event.memberIds,
            fcmNotification = fcmNotification,
        )
    }
}
