package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.BalanceGame
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceGameRepository : JpaRepository<BalanceGame, Long> {
}