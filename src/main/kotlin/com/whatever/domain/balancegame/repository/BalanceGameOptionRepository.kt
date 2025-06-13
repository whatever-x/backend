package com.whatever.domain.balancegame.repository

import com.whatever.domain.balancegame.model.BalanceGameOption
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceGameOptionRepository : JpaRepository<BalanceGameOption, Long> {
}