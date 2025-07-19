package com.whatever.content.service.event

import com.whatever.domain.base.AbstractEntityCleanupService
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.repository.ContentRepository
import org.springframework.stereotype.Service

@Service
class ContentCleanupService(
    private val contentRepository: ContentRepository,
) : AbstractEntityCleanupService<Content>() {

    override fun runCleanup(userId: Long): Int {
        return contentRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
