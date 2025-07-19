package com.whatever.domain.content.tag.service.event

import com.whatever.domain.base.AbstractEntityCleanupService
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import org.springframework.stereotype.Service

@Service
class TagContentMappingCleanupService(
    private val tagContentMappingRepository: TagContentMappingRepository,
) : AbstractEntityCleanupService<TagContentMapping>() {

    override fun runCleanup(userId: Long): Int {
        return tagContentMappingRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
