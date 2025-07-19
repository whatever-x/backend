package com.whatever.domain.content.service.event

import com.whatever.couple.service.event.dto.CoupleMemberLeaveEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger { }

@Component
class ContentEventListener(
    private val contentCleanupService: ContentCleanupService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun deleteAllContent(event: CoupleMemberLeaveEvent) {
        contentCleanupService.cleanupEntity(
            userId = event.userId,
            entityName = ENTITY_NAME
        )
    }

    companion object {
        const val ENTITY_NAME = "Content"
    }
}
