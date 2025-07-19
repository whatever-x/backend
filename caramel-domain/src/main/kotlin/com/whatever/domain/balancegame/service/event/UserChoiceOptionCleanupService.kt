package com.whatever.domain.balancegame.service.event

import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.base.AbstractEntityCleanupService
import com.whatever.domain.content.model.Content
import org.springframework.stereotype.Service

@Service
class UserChoiceOptionCleanupService(
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
) : AbstractEntityCleanupService<Content>() {

    override fun runCleanup(userId: Long): Int {
        return userChoiceOptionRepository.softDeleteAllByUserIdInBulk(userId)
    }
}