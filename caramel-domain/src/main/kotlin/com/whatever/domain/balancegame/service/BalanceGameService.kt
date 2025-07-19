package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.controller.dto.request.ChooseBalanceGameOptionRequest
import com.whatever.domain.balancegame.controller.dto.response.ChooseBalanceGameOptionResponse
import com.whatever.domain.balancegame.controller.dto.response.GetBalanceGameResponse
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_CHANGED
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_NOT_EXISTS
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.GAME_OPTION_NOT_ENOUGH
import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode.ILLEGAL_OPTION
import com.whatever.domain.balancegame.exception.BalanceGameIllegalArgumentException
import com.whatever.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.domain.balancegame.exception.BalanceGameNotFoundException
import com.whatever.domain.balancegame.exception.BalanceGameOptionNotFoundException
import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.exception.ErrorUi
import com.whatever.global.security.util.SecurityUtil.getCurrentUserCoupleId
import com.whatever.global.security.util.SecurityUtil.getCurrentUserId
import com.whatever.caramel.common.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Service
class BalanceGameService(
    private val balanceGameRepository: BalanceGameRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun getTodayBalanceGameInfo(): GetBalanceGameResponse {
        val todayGame = getBalanceGame()
        val sortedOptions = getSortedActiveBalanceGameOptions(todayGame.options)

        val memberChoices = getCoupleMemberChoices(
            coupleId = getCurrentUserCoupleId(),
            game = todayGame,
        )

        val userId = getCurrentUserId()
        return GetBalanceGameResponse.of(
            game = todayGame,
            options = sortedOptions,
            myChoice = memberChoices.find { it.user.id == userId }?.balanceGameOption,
            partnerChoice = memberChoices.find { it.user.id != userId }?.balanceGameOption,
        )
    }

    @Transactional
    fun chooseBalanceGameOption(
        gameId: Long,
        request: ChooseBalanceGameOptionRequest,
    ): ChooseBalanceGameOptionResponse {
        val balanceGame = getBalanceGame()
        if (balanceGame.id != gameId) {
            throw BalanceGameIllegalArgumentException(
                errorCode = GAME_CHANGED,
                errorUi = ErrorUi.Dialog(
                    title = "12시가 넘어 새로운 질문으로 업데이트되었어요.",
                    description = "질문을 보고 새롭게 선택해 주세요."
                )
            )
        }
        val sortedOptions = getSortedActiveBalanceGameOptions(balanceGame.options)

        val coupleId = getCurrentUserCoupleId()
        val requestUserId = getCurrentUserId()
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
            options = sortedOptions,
            myChoice = myChoice.balanceGameOption,
            partnerChoice = partnerChoice?.balanceGameOption,
        )
    }

    private fun getSortedActiveBalanceGameOptions(
        options: List<BalanceGameOption>,
    ): List<BalanceGameOption> {
        val sortedOptions = options.filter { !it.isDeleted }.sortedBy { it.id }
        if (sortedOptions.size < 2) {
            throw BalanceGameIllegalStateException(
                errorCode = GAME_OPTION_NOT_ENOUGH,
                errorUi = ErrorUi.Toast("밸런스 게임의 선택지가 모두 등록되지 않았어요.")
            )
        }
        return sortedOptions
    }

    private fun getBalanceGame(
        date: LocalDate = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate(),
    ): BalanceGame {
        return balanceGameRepository.findWithOptionsByGameDate(gameDate = date)
            ?: throw BalanceGameNotFoundException(errorCode = GAME_NOT_EXISTS)
    }

    private fun getCoupleMemberChoices(
        coupleId: Long,
        game: BalanceGame,
    ): List<UserChoiceOption> {
        val memberIds = coupleRepository.findByIdWithMembers(coupleId)?.members?.map { it.id }
            ?: return emptyList()
        val memberChoices = userChoiceOptionRepository.findAllWithOptionByBalanceGameIdAndUsers(
            gameId = game.id,
            userIds = memberIds,
        )
        return memberChoices
    }

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
