package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Service
class BalanceGameService(
    private val balanceGameRepository: BalanceGameRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
    private val coupleRepository: CoupleRepository
) {

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }

    @Transactional(readOnly = true)
    fun getTodayBalanceGame(): GetBalanceGameResponse {
        val now = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate()

        val todayGame = balanceGameRepository.findByGameDateAndIsDeleted(gameDate = now)
            ?: throw RuntimeException() // TODO(준용) NFE
        if (todayGame.options.size < 2) {
            throw RuntimeException()  // TODO(준용) ISE
        }

        val sortedOptions = todayGame.options
            .filter { !it.isDeleted }
            .sortedBy { it.id }
        val memberChoices = getCoupleMemberChoices(
            coupleId = SecurityUtil.getCurrentUserCoupleId(),
            game = todayGame,
        )

        return GetBalanceGameResponse.of(
            game = todayGame,
            options = sortedOptions,
            myChoice = memberChoices.find { it.user.id == SecurityUtil.getCurrentUserId() },
            partnerChoice = memberChoices.find { it.user.id != SecurityUtil.getCurrentUserId() },
        )
    }

    private fun getCoupleMemberChoices(
        coupleId:Long,
        game: BalanceGame
    ): List<UserChoiceOption> {
        val memberIds = coupleRepository.findByIdWithMembers(coupleId)?.members?.map { it.id }
            ?: emptyList()
        val memberChoices = userChoiceOptionRepository.findByBalanceGame_IdAndUser_IdIn(
            gameId = game.id,
            userIds = memberIds,
        )
        return memberChoices
    }
}