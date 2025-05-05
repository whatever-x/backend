package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.UserChoiceOption
import org.springframework.data.jpa.repository.JpaRepository

interface UserChoiceOptionRepository : JpaRepository<UserChoiceOption, Long> {
    fun findByBalanceGame_IdAndUser_IdInAndIsDeleted(
        gameId: Long,
        userIds: List<Long>,
        isDeleted: Boolean = false,
    ): List<UserChoiceOption>
}