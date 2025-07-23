package com.whatever.caramel.domain.balancegame.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode
import com.whatever.caramel.domain.balancegame.exception.BalanceGameExceptionCode.GAME_NOT_EXISTS
import com.whatever.caramel.domain.balancegame.exception.BalanceGameIllegalArgumentException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameNotFoundException
import com.whatever.caramel.domain.balancegame.exception.BalanceGameOptionNotFoundException
import com.whatever.caramel.domain.balancegame.model.BalanceGame
import com.whatever.caramel.domain.balancegame.model.BalanceGameOption
import com.whatever.caramel.domain.balancegame.model.UserChoiceOption
import com.whatever.caramel.domain.balancegame.repository.BalanceGameOptionRepository
import com.whatever.caramel.domain.balancegame.repository.BalanceGameRepository
import com.whatever.caramel.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.caramel.domain.calendarevent.scheduleevent.service.createCouple
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

@CaramelDomainSpringBootTest
class BalanceGameServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val balanceGameService: BalanceGameService,
    private val balanceGameRepository: BalanceGameRepository,
    private val balanceGameOptionRepository: BalanceGameOptionRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
) {

    @AfterEach
    fun tearDown() {
        userChoiceOptionRepository.deleteAllInBatch()
        balanceGameOptionRepository.deleteAllInBatch()
        balanceGameRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("밸런스 게임을 조회 시 게임의 정보가 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WithNoMemberChoices() {
        // given
        setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()

            // when
            val result = balanceGameService.getTodayBalanceGameInfo()

            // then
            assertThat(result.id).isEqualTo(expectedGame.first.id)
            assertThat(result.options.map { it.id }).containsExactlyInAnyOrderElementsOf(expectedGame.second.map { it.id })
        }
    }

    @DisplayName("밸런스 게임을 조회 시 밸런스 게임이 없는 경우 BalanceGameNotFoundException 을 던진다")
    @Test
    fun getTodayBalanceGameInfo_ButThrowException() {
        // given
        setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            mock<BalanceGameRepository> {
                on { findWithOptionsByGameDate(any()) } doReturn null
            }

            // when
            val result = assertThrows<BalanceGameNotFoundException> {
                balanceGameService.getTodayBalanceGameInfo()
            }

            // then
            assertThat(result.errorCode).isEqualTo(GAME_NOT_EXISTS)
            assertThat(result.errorUi.title).isEqualTo("밸런스 게임을 찾을 수 없어요.")
        }
    }

    @DisplayName("밸런스 게임 커플 선택 조회 시, 커플 멤버중 나만 선택했을 경우 내 선택 정보가 반환된다.")
    @Test
    fun getCoupleMemberChoices_WhenIHaveChosen() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()
            val myChoice = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = expectedGame.first,
                    balanceGameOption = expectedGame.second.first(),
                    user = myUser,
                )
            )

            // when
            val result = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

            // then
            val myChoiceOption = result.first { it.userId == myUser.id }
            val partnerChoiceOption = result.firstOrNull { it.userId != myUser.id }

            assertThat(myChoiceOption.balanceGameOptionId).isEqualTo(myChoice.balanceGameOption.id)
            assertThat(partnerChoiceOption).isNull()
        }
    }

    @DisplayName("밸런스 게임 커플 선택 조회 시, 커플 멤버중 파트너만 선택했을 경우 파트너 선택 정보가 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WhenPartnerChosen() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()
            val partnerChoice = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = expectedGame.first,
                    balanceGameOption = expectedGame.second.first(),
                    user = partnerUser,
                )
            )

            // when
            val result = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

            // then
            val myChoiceOption = result.firstOrNull { it.userId == myUser.id }
            val partnerChoiceOption = result.first { it.userId == partnerUser.id }
            assertThat(myChoiceOption).isNull()
            assertThat(partnerChoiceOption.balanceGameOptionId).isEqualTo(partnerChoice.balanceGameOption.id)
        }
    }

    @DisplayName("밸런스 게임을 조회 시 커플멤버 모두 선택했을 경우 선택 정보도 함께 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WhenBothMembersChosen() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()
            val myChoice = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = expectedGame.first,
                    balanceGameOption = expectedGame.second.last(),
                    user = myUser,
                )
            )
            val partnerChoice = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = expectedGame.first,
                    balanceGameOption = expectedGame.second.first(),
                    user = partnerUser,
                )
            )

            // when
            val result = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

            // then
            val myChoiceOption = result.first { it.userId == myUser.id }
            val partnerChoiceOption = result.first { it.userId == partnerUser.id }
            assertThat(myChoiceOption.balanceGameOptionId).isEqualTo(myChoice.balanceGameOption.id)
            assertThat(partnerChoiceOption.balanceGameOptionId).isEqualTo(partnerChoice.balanceGameOption.id)
        }
    }

    @DisplayName("커플의 밸런스 게임 선택 조회시, Couple Id로 조회했는데 없는 경우 emptyList 를 반환한다")
    @Test
    fun getCoupleMemberChoices_WhenFindByIdWithMembersReturnsNull() {
        // given
        val (_, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()
        val coupleRepository = mock<CoupleRepository>()
        mock<CoupleRepository> {
            on { findByIdWithMembers(anyLong()) } doReturn null
        }
        whenever(coupleRepository.findByIdWithMembers(anyLong())).doReturn(null)
        val balanceGameService =
            BalanceGameService(balanceGameRepository, userChoiceOptionRepository, coupleRepository, userRepository)

        // when
        val userChoices = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

        // then
        assertThat(userChoices.size).isEqualTo(0)
    }

    @DisplayName("밸런스 게임을 조회 시 선택지가 두개 미만일 경우 예외가 발생한다.")
    @Test
    fun getTodayBalanceGameInfo_WithIllegalOptionCount() {
        // given
        val (_, _, _) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameList = makeBalanceGame(1, now.toLocalDate())
            balanceGameOptionRepository.delete(gameList.first().second.last())

            // when
            val result = assertThrows<BalanceGameIllegalStateException> {
                balanceGameService.getTodayBalanceGameInfo()
            }

            // then
            assertThat(result.errorCode).isEqualTo(BalanceGameExceptionCode.GAME_OPTION_NOT_ENOUGH)
        }
    }

    @DisplayName("밸런스게임 선택 시 아무도 입력하지 않았다면 나의 선택 결과만 반환된다.")
    @Test
    fun chooseBalanceGameOption_WithNoMemberChoices() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()
            val gameId = gameInfo.first.id
            val selectedOptionId = gameInfo.second.first().id

            // when
            val result = balanceGameService.chooseBalanceGameOption(
                gameId = gameId,
                selectedOptionId = selectedOptionId,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            assertThat(result.myChoice).isNotNull
            assertThat(result.myChoice!!.balanceGameId).isEqualTo(gameId)
            assertThat(result.myChoice!!.balanceGameOptionId).isEqualTo(selectedOptionId)
            assertThat(result.partnerChoice).isNull()
        }
    }

    @DisplayName("밸런스게임 선택 시 내가 이미 선택했다면 초기 선택 결과가 반환된다.")
    @Test
    fun chooseBalanceGameOption_WhenIHaveChosen() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()
            val firstChoiceOption = gameInfo.second.first()
            val myChoiceOption = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = gameInfo.first,
                    balanceGameOption = firstChoiceOption,
                    user = myUser,
                )
            )

            val gameId = gameInfo.first.id
            val selectedOptionId = gameInfo.second.last().id  // select a different option-id

            // when
            val result = balanceGameService.chooseBalanceGameOption(
                gameId = gameId,
                selectedOptionId = selectedOptionId,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            assertThat(result.myChoice).isNotNull
            assertThat(result.myChoice!!.balanceGameId).isEqualTo(myChoiceOption.balanceGame.id)
            assertThat(result.myChoice!!.balanceGameOptionId).isEqualTo(myChoiceOption.balanceGameOption.id)
            assertThat(result.partnerChoice).isNull()
        }
    }

    @DisplayName("밸런스게임 선택 시 파트너가 이미 선택했다면 커플멤버 모두의 선택 결과가 반환된다.")
    @Test
    fun chooseBalanceGameOption_WhenPartnerChosen() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()
            val firstChoiceOption = gameInfo.second.first()
            val partnerChoiceOption = userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = gameInfo.first,
                    balanceGameOption = firstChoiceOption,
                    user = partnerUser,
                )
            )

            val myChoiceOption = gameInfo.second.last()
            val gameId = gameInfo.first.id
            val selectedOptionId = myChoiceOption.id

            // when
            val result = balanceGameService.chooseBalanceGameOption(
                gameId = gameId,
                selectedOptionId = selectedOptionId,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            assertThat(result.myChoice).isNotNull
            assertThat(result.myChoice!!.balanceGameId).isEqualTo(gameId)
            assertThat(result.myChoice!!.balanceGameOptionId).isEqualTo(selectedOptionId)

            assertThat(result.partnerChoice).isNotNull
            with(partnerChoiceOption) {
                assertThat(result.partnerChoice!!.balanceGameId).isEqualTo(balanceGame.id)
                assertThat(result.partnerChoice!!.balanceGameOptionId).isEqualTo(balanceGameOption.id)
            }
        }
    }

    @DisplayName("밸런스게임 조회 후 자정을 지나 선택했다면 게임이 바뀌어 예외가 발생한다.")
    @Test
    fun chooseBalanceGameOption_WhenOverMidnight() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val before = LocalDateTime.of(2025, 5, 4, 23, 59, 59)
        val now = LocalDateTime.of(2025, 5, 5, 0, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any()))
                .thenReturn(before)
                .thenReturn(now)
            makeBalanceGame(2, before.toLocalDate())
            val beforeGame = balanceGameService.getTodayBalanceGameInfo()

            val beforeGameId = beforeGame.id
            val selectedOptionId = beforeGame.options.first().id

            // when
            val result = assertThrows<BalanceGameIllegalArgumentException> {
                balanceGameService.chooseBalanceGameOption(
                    gameId = beforeGame.id,
                    selectedOptionId = selectedOptionId,
                    coupleId = couple.id,
                    requestUserId = myUser.id,
                )
            }

            // then
            assertThat(result.errorCode).isEqualTo(BalanceGameExceptionCode.GAME_CHANGED)
        }
    }

    @DisplayName("밸런스게임의 선택지가 아닌 id를 잘못 요청하면 예외가 발생한다.")
    @Test
    fun chooseBalanceGameOption_WithIllegalOptionId() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 0, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            makeBalanceGame(1, now.toLocalDate())
            val beforeGame = balanceGameService.getTodayBalanceGameInfo()
            val gameId = beforeGame.id
            val illegalOptionId = 0L

            // when
            val result = assertThrows<BalanceGameOptionNotFoundException> {
                balanceGameService.chooseBalanceGameOption(
                    gameId = gameId,
                    selectedOptionId = illegalOptionId,
                    coupleId = couple.id,
                    requestUserId = myUser.id,
                )
            }

            // then
            assertThat(result.errorCode).isEqualTo(BalanceGameExceptionCode.ILLEGAL_OPTION)
        }
    }

    private fun setUpCouple(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id",
    ): Triple<com.whatever.caramel.domain.user.model.User, com.whatever.caramel.domain.user.model.User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformId,
            partnerPlatformId
        )
        return Triple(myUser, partnerUser, couple)
    }

    /**
     * @param count 시작일로부터 count개수 만큼의 게임을 만듭니다.
     * @param startGameDate 게임을 생성할 시작일을 지정합니다.
     */
    private fun makeBalanceGame(
        count: Int,
        startGameDate: LocalDate,
    ): ArrayList<Pair<BalanceGame, List<BalanceGameOption>>> {
        val gameList = arrayListOf<Pair<BalanceGame, List<BalanceGameOption>>>()
        for (i in 0..(count - 1)) {
            val gameDate = startGameDate.plusDays(i.toLong())
            val question = "question: ${i}"
            val option1 = "option: ${i}-1"
            val option2 = "option: ${i}-2"
            val savedGame = balanceGameRepository.save(
                BalanceGame(
                    gameDate = gameDate,
                    question = question,
                )
            )
            val savedOptions = balanceGameOptionRepository.saveAll(
                listOf(
                    BalanceGameOption(optionText = option1, balanceGame = savedGame),
                    BalanceGameOption(optionText = option2, balanceGame = savedGame),
                )
            )
            gameList.add(Pair(savedGame, savedOptions))
        }
        return gameList
    }
}
