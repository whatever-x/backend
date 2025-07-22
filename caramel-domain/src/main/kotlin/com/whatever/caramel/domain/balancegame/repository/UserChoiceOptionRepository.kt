package com.whatever.caramel.domain.balancegame.repository

import com.whatever.caramel.domain.balancegame.model.UserChoiceOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserChoiceOptionRepository : JpaRepository<UserChoiceOption, Long> {
    fun findByBalanceGame_IdAndUser_IdInAndIsDeleted(
        gameId: Long,
        userIds: List<Long>,
        isDeleted: Boolean = false,
    ): List<UserChoiceOption>

    @Query(
        """
        select uco from UserChoiceOption uco
            join fetch uco.balanceGameOption
        where uco.balanceGame.id = :gameId
            and uco.user.id in :userIds
            and uco.isDeleted = false 
    """
    )
    fun findAllWithOptionByBalanceGameIdAndUsers(
        gameId: Long,
        userIds: List<Long>,
        isDeleted: Boolean = false,
    ): List<UserChoiceOption>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update UserChoiceOption uco
        set uco.isDeleted = true
        where uco.user.id = :userId
            and uco.isDeleted = false
    """
    )
    fun softDeleteAllByUserIdInBulk(userId: Long): Int
}
