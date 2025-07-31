package com.whatever.caramel.domain.balancegame.service.event

import com.whatever.caramel.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.caramel.domain.base.AbstractEntityCleanupService
import com.whatever.caramel.domain.content.model.Content
import org.springframework.stereotype.Service

@Service
class UserChoiceOptionCleanupService(
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
) : AbstractEntityCleanupService<Content>() {

    override fun runCleanup(userId: Long): Int {
        return userChoiceOptionRepository.softDeleteAllByUserIdInBulk(userId)
    }
}
