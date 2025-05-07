package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.util.DateTimeUtil
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.ZoneId

interface BalanceGameRepository : JpaRepository<BalanceGame, Long> {
    fun findByGameDateAndIsDeleted(gameDate: LocalDate, isDeleted: Boolean = false): BalanceGame?
}