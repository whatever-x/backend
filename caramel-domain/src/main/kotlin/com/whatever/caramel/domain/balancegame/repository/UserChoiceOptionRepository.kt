package com.whatever.caramel.domain.balancegame.repository

import com.whatever.caramel.domain.balancegame.model.BalanceGame
import com.whatever.caramel.domain.balancegame.model.UserChoiceOption
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

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
        userIds: Set<Long>,
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

    @Query(
        """
            select bg.id from UserChoiceOption uco
                join uco.balanceGame bg
            where uco.user.id in :memberIds
                and uco.isDeleted = false
                and bg.isDeleted = false
            group by bg.id
            order by bg.gameDate desc
        """
    )
    fun findRespondedGameIds(
        memberIds: Collection<Long>,
        pageable: Pageable,
    ): List<Long>

    @Query(
        """
            select bg.id from UserChoiceOption uco
                join uco.balanceGame bg
            where uco.user.id in :memberIds
                and uco.isDeleted = false
                and bg.isDeleted = false
                and bg.gameDate < :cursor
            group by bg.id
            order by bg.gameDate desc
        """
    )
    fun findRespondedGameIdsBefore(
        memberIds: Collection<Long>,
        cursor: LocalDate,
        pageable: Pageable,
    ): List<Long>

    @Query(
        """
            select uco from UserChoiceOption uco
            where uco.balanceGame.id in :gameIds
                and uco.user.id in :memberIds
                and uco.isDeleted = false
        """
    )
    fun findAllByGameIdsAndUserIds(
        gameIds: Collection<Long>,
        memberIds: Collection<Long>
    ): List<UserChoiceOption>
}
