package com.whatever.caramel.domain.content.service.event

import com.whatever.caramel.domain.base.AbstractEntityCleanupService
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.repository.ContentRepository
import org.springframework.stereotype.Service

@Service
class ContentCleanupService(
    private val contentRepository: ContentRepository,
) : AbstractEntityCleanupService<Content>() {

    override fun runCleanup(userId: Long): Int {
        return contentRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
