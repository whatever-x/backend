package com.whatever.domain.content.tag.service.event

import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.couple.service.event.dto.CoupleMemberLeaveEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val logger = KotlinLogging.logger {  }

@Component
class TagContentMappingEventListener(
    private val tagContentMappingRepository: TagContentMappingRepository
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    fun deleteAllTagContentMapping(event: CoupleMemberLeaveEvent) {
        val effectedRow = tagContentMappingRepository.softDeleteAllByUserIdInBulk(event.userId)
        logger.info { "${effectedRow} delete TagContentMapping" }
    }
}