package com.whatever.caramel.domain.balancegame.repository

import com.whatever.caramel.domain.balancegame.model.BalanceGameOption
import org.springframework.data.jpa.repository.JpaRepository

interface BalanceGameOptionRepository : JpaRepository<BalanceGameOption, Long> {
}
