package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.domain.balancegame.controller.dto.response.ChooseBalanceGameOptionResponse
import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
class BalanceGameService(
    private val balanceGameRepository: BalanceGameRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository
) {

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }

    @Transactional(readOnly = true)
    fun getTodayBalanceGameInfo(): GetBalanceGameResponse {
        val todayGame = getBalanceGame()
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

    @Transactional
    fun chooseBalanceGameOption(request: ChooseBalanceGameOptionRequest): ChooseBalanceGameOptionResponse {
        val balanceGame = getBalanceGame()
        if (balanceGame.id != request.gameId) {
            throw RuntimeException()  // TODO(준용) IAE-잘못된 게임 선택(게임 변경됨)
        }

        val coupleId = SecurityUtil.getCurrentUserCoupleId()
        val requestUserId = SecurityUtil.getCurrentUserId()
        val memberChoices = getCoupleMemberChoices(
            coupleId = coupleId,
            game = balanceGame,
        )

        val myChoice = memberChoices.find { it.user.id == requestUserId }
        val partnerChoice = memberChoices.find { it.user.id != requestUserId }
        if (myChoice != null) {
            return ChooseBalanceGameOptionResponse.of(
                game = balanceGame,
                myChoice = myChoice,
                partnerChoice = partnerChoice,
            )
        }
        
        val selectedOption = balanceGame.options.find { it.id == request.optionId }
            ?: throw RuntimeException() // TODO(준용) IAE-잘못된 옵션 선택

        val requestUser = userRepository.getReferenceById(requestUserId)
        val newChoice = UserChoiceOption(
            balanceGame = balanceGame,
            balanceGameOption = selectedOption,
            user = requestUser,
        )
        val savedChoice = userChoiceOptionRepository.save(newChoice)

        return ChooseBalanceGameOptionResponse.of(
            game = balanceGame,
            myChoice = savedChoice,
            partnerChoice = partnerChoice,
        )
    }

    private fun getBalanceGame(
        date: LocalDate = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate()
    ): BalanceGame {
        return balanceGameRepository.findByGameDateAndIsDeleted(gameDate = date)
            ?: throw RuntimeException() // TODO(준용) NFE
    }

    private fun getCoupleMemberChoices(
        coupleId:Long,
        game: BalanceGame,
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