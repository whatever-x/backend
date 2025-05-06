package com.whatever.domain.content.service.event

import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {  }

@Component
class ContentEventListener(
    private val contentRepository: ContentRepository
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    fun deleteAllContent(event: CoupleMemberLeaveEvent) {
        val effectedRow = contentRepository.softDeleteAllByUserIdInBulk(event.userId)
        logger.info { "${effectedRow} delete Content" }
    }
}