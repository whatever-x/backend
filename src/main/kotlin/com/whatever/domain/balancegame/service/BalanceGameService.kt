package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.domain.balancegame.controller.dto.response.ChooseBalanceGameOptionResponse
import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_CHANGED
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_NOT_EXISTS
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_OPTION_NOT_ENOUGH
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.ILLEGAL_OPTION
import com.whatever.domain.balancegame.exception.BalanceGameIllegalArgumentException
import com.whatever.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.domain.balancegame.exception.BalanceGameNotFoundException
import com.whatever.domain.balancegame.exception.BalanceGameOptionNotFoundException
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
            throw BalanceGameIllegalStateException(errorCode = GAME_OPTION_NOT_ENOUGH)
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
            throw BalanceGameIllegalArgumentException(errorCode = GAME_CHANGED)
        }

        val coupleId = SecurityUtil.getCurrentUserCoupleId()
        val requestUserId = SecurityUtil.getCurrentUserId()
        val memberChoices = getCoupleMemberChoices(
            coupleId = coupleId,
            game = balanceGame,
        )

        val partnerChoice = memberChoices.find { it.user.id != requestUserId }
        val myChoice = memberChoices.find { it.user.id == requestUserId }
            ?: run {
                val selectedOption = balanceGame.options.find { it.id == request.optionId }
                    ?: throw BalanceGameOptionNotFoundException(errorCode = ILLEGAL_OPTION)

                val requestUser = userRepository.getReferenceById(requestUserId)
                val newChoice = UserChoiceOption(
                    balanceGame = balanceGame,
                    balanceGameOption = selectedOption,
                    user = requestUser,
                )
                userChoiceOptionRepository.save(newChoice)
            }

        return ChooseBalanceGameOptionResponse.of(
            game = balanceGame,
            myChoice = myChoice,
            partnerChoice = partnerChoice,
        )
    }

    private fun getBalanceGame(
        date: LocalDate = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate()
    ): BalanceGame {
        return balanceGameRepository.findByGameDateAndIsDeleted(gameDate = date)
            ?: throw BalanceGameNotFoundException(errorCode = GAME_NOT_EXISTS)
    }

    private fun getCoupleMemberChoices(
        coupleId:Long,
        game: BalanceGame,
    ): List<UserChoiceOption> {
        val memberIds = coupleRepository.findByIdWithMembers(coupleId)?.members?.map { it.id }
            ?: return emptyList()
        val memberChoices = userChoiceOptionRepository.findByBalanceGame_IdAndUser_IdInAndIsDeleted(
            gameId = game.id,
            userIds = memberIds,
        )
        return memberChoices
    }
}