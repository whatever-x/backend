package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.BalanceGame
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface BalanceGameRepository : JpaRepository<BalanceGame, Long> {
    fun findByGameDateAndIsDeleted(gameDate: LocalDate, isDeleted: Boolean = false): BalanceGame?

    @Query("""
        select bg from BalanceGame bg
            join fetch bg.options
        where bg.gameDate = :gameDate
            and bg.isDeleted = false
    """)
    fun findWithOptionsByGameDate(gameDate: LocalDate): BalanceGame?
}