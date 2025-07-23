package com.whatever.caramel.domain.balancegame.service

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.GAME_CHANGED
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.GAME_NOT_EXISTS
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.ILLEGAL_OPTION
import com.whatever.caramel.domain.balancegame.exception.BalanceGameIllegalArgumentException
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

        val memberChoices = getCoupleMemberChoices(
            coupleId = coupleId,
            gameId = balanceGame.id,
        )

        val partnerChoice = memberChoices.find { it.userId != requestUserId }
        val myChoice = memberChoices.find { it.userId == requestUserId }
            ?: run {
                val selectedOption = balanceGame.options.find { it.id == selectedOptionId }
                    ?: throw BalanceGameOptionNotFoundException(errorCode = ILLEGAL_OPTION)

                val requestUser = userRepository.getReferenceById(requestUserId)
                val newChoice = UserChoiceOption(
                    balanceGame = balanceGame,
                    balanceGameOption = selectedOption,
                    user = requestUser,
                )

                userChoiceOptionRepository.save(newChoice).run {
                    UserChoiceOptionVo.from(this)
                }
            }

        return CoupleChoiceOptionVo.from(
            myChoice = myChoice,
            partnerChoice = partnerChoice,
        )
    }

    /**
     * VO로 이전
     * 병합 후 삭제
     */
//    private fun getSortedActiveBalanceGameOptions(
//        options: List<BalanceGameOption>,
//    ): List<BalanceGameOption> {
//        val sortedOptions = options.filter { !it.isDeleted }.sortedBy { it.id }
//        if (sortedOptions.size < 2) {
//            throw BalanceGameIllegalStateException(
//                errorCode = GAME_OPTION_NOT_ENOUGH,
//                errorUi = ErrorUi.Toast("밸런스 게임의 선택지가 모두 등록되지 않았어요.")
//            )
//        }
//        return sortedOptions
//    }

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
        val memberIds = coupleRepository.findByIdWithMembers(coupleId)?.members?.map { it.id }
            ?: return emptyList()
        val memberChoices = userChoiceOptionRepository.findAllWithOptionByBalanceGameIdAndUsers(
            gameId = gameId,
            userIds = memberIds,
        )
        return memberChoices.map { UserChoiceOptionVo.from(it) }
    }

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
