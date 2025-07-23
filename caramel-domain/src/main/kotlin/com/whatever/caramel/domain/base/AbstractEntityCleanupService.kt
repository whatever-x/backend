package com.whatever.caramel.domain.base

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.support.RetrySynchronizationManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger { }

abstract class AbstractEntityCleanupService<E : BaseEntity> {

    /**
     * 해당 도메인 entity를 모두 Soft Delete하고, 영향을 받은 row를 반환하는 함수
     * @param userId 삭제를 진행할 entity의 소유자
     * @return 삭제에 영향을 받은 row 수
     */
    protected abstract fun runCleanup(userId: Long): Int

    @Retryable(
        retryFor = [DataAccessException::class],
        backoff = Backoff(delay = 100, maxDelay = 300),
        maxAttempts = 3,
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun cleanupEntity(userId: Long, entityName: String): Int {
        val effectedRow = runCleanup(userId)

        val attemptCount = RetrySynchronizationManager.getContext()?.retryCount ?: 0
        logger.info { "[$effectedRow ${entityName}] soft-deleted for userId: $userId on attempt ${attemptCount + 1}" }

        if (attemptCount > 0 && effectedRow > 0) {  // 재시도 후 성공했을 경우에만 로깅 진행
            logger.warn { "Successfully soft-deleted $entityName for userId: $userId after ${attemptCount + 1} attempts." }
        }

        return effectedRow
    }

    @Recover
    fun recoverCleanupEntity(ex: Throwable, userId: Long, entityName: String): Int {
        logger.error { "Failed to soft-delete $entityName for userId: $userId after multiple retries." }
        throw ex
    }
}
