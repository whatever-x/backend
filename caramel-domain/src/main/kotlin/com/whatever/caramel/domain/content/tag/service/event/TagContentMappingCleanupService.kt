package com.whatever.caramel.domain.content.tag.service.event

import com.whatever.caramel.domain.base.AbstractEntityCleanupService
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import org.springframework.stereotype.Service

@Service
class TagContentMappingCleanupService(
    private val tagContentMappingRepository: TagContentMappingRepository,
) : AbstractEntityCleanupService<TagContentMapping>() {

    override fun runCleanup(userId: Long): Int {
        return tagContentMappingRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
