package com.whatever.caramel.domain.balancegame.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.ALREADY_PICKED
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.GAME_CHANGED
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.GAME_NOT_EXISTS
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.ILLEGAL_OPTION
import com.whatever.caramel.domain.balancegame.exception.BalanceGameIllegalArgumentException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameNotFoundException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameOptionNotFoundException
import com.whatever.caramel.domain.balancegame.model.BalanceGame
import com.whatever.caramel.domain.balancegame.model.UserChoiceOption
import com.whatever.caramel.domain.balancegame.repository.BalanceGameRepository
import com.whatever.caramel.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.caramel.domain.balancegame.vo.BalanceGameVo
import com.whatever.caramel.domain.balancegame.vo.CoupleChoiceOptionVo
import com.whatever.caramel.domain.balancegame.vo.UserChoiceOptionVo
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.repository.UserRepository
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
    fun getTodayBalanceGameInfo(): BalanceGameVo {
        val todayGame = getBalanceGame()
        return BalanceGameVo.from(todayGame, todayGame.options)
    }

    @Transactional
    fun chooseBalanceGameOption(
        gameId: Long,
        selectedOptionId: Long,
        coupleId: Long,
        requestUserId: Long,
    ): CoupleChoiceOptionVo {
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

        val memberChoices = getCoupleMemberChoicesEntity(
            coupleId = coupleId,
            gameId = balanceGame.id,
        )
        val selectedOption = balanceGame.options.find { it.id == selectedOptionId }
            ?: throw BalanceGameOptionNotFoundException(errorCode = ILLEGAL_OPTION)

        val partnerChoiceVo = memberChoices.find { it.user.id != requestUserId }?.let {
            UserChoiceOptionVo.from(it)
        }

        val myChoice = memberChoices.find { it.user.id == requestUserId }
            ?.let { userChoiceOption ->
                // 추후에 PATCH 분리시 이곳을 분리해야함
                if (partnerChoiceVo != null) {
                    throw BalanceGameIllegalStateException(
                        errorCode = ALREADY_PICKED,
                        errorUi = ErrorUi.Toast("파트너의 선택이 완료되어 변경할 수 없어요"),
                    )
                }
                userChoiceOption.apply {
                    balanceGameOption = selectedOption
                }
            } ?: run {
            val requestUser = userRepository.getReferenceById(requestUserId)
            val newChoice = UserChoiceOption(
                balanceGame = balanceGame,
                balanceGameOption = selectedOption,
                user = requestUser,
            )

            userChoiceOptionRepository.save(newChoice)
        }

        val myChoiceVo = UserChoiceOptionVo.from(myChoice)

        return CoupleChoiceOptionVo.from(
            myChoice = myChoiceVo,
            partnerChoice = partnerChoiceVo,
        )
    }

    private fun getBalanceGame(
        date: LocalDate = DateTimeUtil.localNow(TARGET_ZONE_ID).toLocalDate(),
    ): BalanceGame {
        return balanceGameRepository.findWithOptionsByGameDate(gameDate = date)
            ?: throw BalanceGameNotFoundException(errorCode = GAME_NOT_EXISTS)
    }

    fun getCoupleMemberChoices(
        coupleId: Long,
        gameId: Long,
    ): List<UserChoiceOptionVo> {
        return getCoupleMemberChoicesEntity(coupleId, gameId).map {
            UserChoiceOptionVo.from(it)
        }
    }

    private fun getCoupleMemberChoicesEntity(
        coupleId: Long,
        gameId: Long,
    ): List<UserChoiceOption> {
        val couple = coupleRepository.findByIdWithMembers(coupleId) ?: return emptyList()
        val memberIds = couple.members.map { it.id }.ifEmpty { return emptyList() }
        return userChoiceOptionRepository.findAllWithOptionByBalanceGameIdAndUsers(
            gameId = gameId,
            userIds = memberIds,
        )
    }

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
