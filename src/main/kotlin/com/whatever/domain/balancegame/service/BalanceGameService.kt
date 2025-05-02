package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Service
class BalanceGameService(private val balanceGameRepository: BalanceGameRepository) {

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }

    @Transactional(readOnly = true)
    fun getTodayBalanceGame(): GetBalanceGameResponse {
        val now = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate()

        val todayGame = balanceGameRepository.findByGameDateAndIsDeleted(gameDate = now)
            ?: throw RuntimeException() // NFE
        if (todayGame.options.size < 2) {
            throw RuntimeException()  // ISE
        }

        val sortedOptions = todayGame.options
            .filter { !it.isDeleted }
            .sortedBy { it.id }
        return GetBalanceGameResponse.of(
            game = todayGame,
            options = sortedOptions
        )
    }
}