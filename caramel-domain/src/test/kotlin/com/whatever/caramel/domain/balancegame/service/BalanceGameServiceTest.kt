package com.whatever.caramel.domain.balancegame.service

import com.whatever.caramel.common.util.CursorUtil
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
import com.whatever.caramel.domain.balancegame.vo.BalanceGameHistoryQueryVo
import com.whatever.caramel.domain.balancegame.vo.BalanceGameHistorySortType
import com.whatever.caramel.domain.balancegame.vo.BalanceGameHistoryVo
import com.whatever.caramel.domain.calendarevent.scheduleevent.service.createCouple
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.contracts.contract
import kotlin.test.Test
import kotlin.test.assertNotNull

@CaramelDomainSpringBootTest
class BalanceGameServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val balanceGameService: BalanceGameService,
    private val balanceGameRepository: BalanceGameRepository,
    private val balanceGameOptionRepository: BalanceGameOptionRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
) : ExcludeAsyncConfigBean() {

    @MockitoBean
    private lateinit var firebaseService: FirebaseService

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
        whenever(coupleRepository.findByIdWithMembers(anyLong())).thenReturn(null)

        val balanceGameService =
            BalanceGameService(balanceGameRepository, userChoiceOptionRepository, coupleRepository, userRepository, mock<ApplicationEventPublisher>())

        // when
        val userChoices = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

        // then
        assertThat(userChoices).isEmpty()
    }

    @DisplayName("커플의 밸런스 게임 선택 조회시, Couple 안의 MemberId 가 빈 경우 emptyList 를 반환한다")
    @Test
    fun getCoupleMemberChoices_WhenMembersIsEmpty() {
        // given
        val (user1, user2, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()
        val coupleRepository = mock<CoupleRepository>()
        whenever(coupleRepository.findByIdWithMembers(anyLong())).thenReturn(couple.apply {
            removeMember(user1)
            removeMember(user2)
        })
        val balanceGameService =
            BalanceGameService(balanceGameRepository, userChoiceOptionRepository, coupleRepository, userRepository, mock<ApplicationEventPublisher>())

        // when
        val userChoices = balanceGameService.getCoupleMemberChoices(couple.id, expectedGame.first.id)

        // then
        assertThat(userChoices).isEmpty()
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
            val myChoice = assertNotNull(result.myChoice)
            assertThat(myChoice.balanceGameId).isEqualTo(gameId)
            assertThat(myChoice.balanceGameOptionId).isEqualTo(selectedOptionId)
            assertThat(result.partnerChoice).isNull()
        }
    }

    @DisplayName("밸런스게임 선택 시 내가 이미 선택했다면 변경된 선택 결과가 반환된다.")
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
            val myChoice = assertNotNull(result.myChoice)
            assertThat(myChoice.balanceGameId).isEqualTo(myChoiceOption.balanceGame.id)
            assertThat(myChoice.balanceGameOptionId).isEqualTo(selectedOptionId)
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

    @DisplayName("밸런스게임을 처음 선택하면 상대방에게 fcm 알림이 전송된다.")
    @Test
    fun chooseBalanceGameOption_WhenFirstChoice_SendsNotificationToPartner() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()

            // when
            balanceGameService.chooseBalanceGameOption(
                gameId = gameInfo.first.id,
                selectedOptionId = gameInfo.second.first().id,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            verify(firebaseService, times(1))
                .sendNotification(
                    targetUserIds = eq(setOf(partnerUser.id)),
                    fcmNotification = any(),
                )
        }
    }

    @DisplayName("밸런스게임을 재선택(이미 내 선택 존재)하면 fcm 알림이 전송되지 않는다.")
    @Test
    fun chooseBalanceGameOption_WhenReChoice_DoesNotSendNotification() {
        // given
        val (myUser, _, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()
            userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = gameInfo.first,
                    balanceGameOption = gameInfo.second.first(),
                    user = myUser,
                )
            )

            // when
            balanceGameService.chooseBalanceGameOption(
                gameId = gameInfo.first.id,
                selectedOptionId = gameInfo.second.last().id,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            verify(firebaseService, never())
                .sendNotification(any(), any())
        }
    }

    @DisplayName("파트너가 먼저 선택한 뒤 내가 처음 선택하면 상대방에게 fcm 알림이 전송된다.")
    @Test
    fun chooseBalanceGameOption_WhenPartnerChosenThenIChooseFirst_SendsNotificationToPartner() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val gameInfo = makeBalanceGame(1, now.toLocalDate()).first()
            userChoiceOptionRepository.save(
                UserChoiceOption(
                    balanceGame = gameInfo.first,
                    balanceGameOption = gameInfo.second.first(),
                    user = partnerUser,
                )
            )

            // when
            balanceGameService.chooseBalanceGameOption(
                gameId = gameInfo.first.id,
                selectedOptionId = gameInfo.second.last().id,
                coupleId = couple.id,
                requestUserId = myUser.id,
            )

            // then
            verify(firebaseService, times(1))
                .sendNotification(
                    targetUserIds = eq(setOf(partnerUser.id)),
                    fcmNotification = any(),
                )
        }
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 응답한 게임들이 gameDate 내림차순으로 반환된다.")
    @Test
    fun getBalanceGameHistory_FirstPageReturnsGamesInGameDateDesc() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val startDate = LocalDate.of(2025, 5, 1)
        val games = makeBalanceGame(3, startDate) // 2025-05-01, 05-02, 05-03
        games.forEach { (game, options) ->
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.first(), user = myUser)
            )
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.last(), user = partnerUser)
            )
        }
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).hasSize(3)
        assertThat(result.list.map { it.balanceGame.gameDate }).containsExactly(
            LocalDate.of(2025, 5, 3),
            LocalDate.of(2025, 5, 2),
            LocalDate.of(2025, 5, 1),
        )
        assertThat(result.cursor.next).isNull()
        result.list.forEach { history ->
            val choice = history.coupleChoiceOption
            assertThat(choice.myChoice).isNotNull
            assertThat(choice.myChoice!!.userId).isEqualTo(myUser.id)
            assertThat(choice.partnerChoice).isNotNull
            assertThat(choice.partnerChoice!!.userId).isEqualTo(partnerUser.id)
        }
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 응답한 게임이 size보다 많으면 다음 커서를 반환한다.")
    @Test
    fun getBalanceGameHistory_WhenMoreThanPageSizeReturnsNextCursor() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val games = makeBalanceGame(5, LocalDate.of(2025, 5, 1)) // 05-01 ~ 05-05
        games.forEach { (game, options) ->
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.first(), user = myUser)
            )
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.last(), user = partnerUser)
            )
        }
        val queryVo = BalanceGameHistoryQueryVo(
            size = 3,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).hasSize(3)
        assertThat(result.list.map { it.balanceGame.gameDate }).containsExactly(
            LocalDate.of(2025, 5, 5),
            LocalDate.of(2025, 5, 4),
            LocalDate.of(2025, 5, 3),
        )
        val lastGameDate = result.list.last().balanceGame.gameDate
        assertThat(result.cursor.next).isEqualTo(CursorUtil.toHash(lastGameDate.toString()))
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 커서로 모든 페이지를 순차 조회하면 전체를 gameDate 내림차순으로 빠짐없이 수집한다.")
    @Test
    fun getBalanceGameHistory_PaginationFetchAllPagesSequentially() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val totalItems = 7
        val pageSize = 3
        val games = makeBalanceGame(totalItems, LocalDate.of(2025, 5, 1))
        games.forEach { (game, options) ->
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.first(), user = myUser)
            )
            userChoiceOptionRepository.save(
                UserChoiceOption(balanceGame = game, balanceGameOption = options.last(), user = partnerUser)
            )
        }
        val expectedDatesDesc = games.map { it.first.gameDate }.sortedDescending()

        val fetched = mutableListOf<BalanceGameHistoryVo>()
        var currentCursor: String? = null
        var pagesFetched = 0
        val maxPages = (totalItems + pageSize - 1) / pageSize

        // when
        do {
            pagesFetched++
            if (pagesFetched > maxPages + 1) {
                throw IllegalStateException("예상보다 많은 페이지($pagesFetched)를 조회했습니다. 커서 로직 확인 필요.")
            }

            val queryVo = BalanceGameHistoryQueryVo(
                size = pageSize,
                cursor = currentCursor,
                sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
            )
            val response = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

            fetched.addAll(response.list)
            currentCursor = response.cursor.next

            if (currentCursor != null) {
                assertThat(response.list).hasSize(pageSize)
                assertThat(response.cursor.next)
                    .isEqualTo(CursorUtil.toHash(response.list.last().balanceGame.gameDate.toString()))
            } else {
                val expectedLastPageSize = if (totalItems % pageSize == 0) pageSize else totalItems % pageSize
                assertThat(response.list).hasSize(expectedLastPageSize)
            }
        } while (currentCursor != null)

        // then
        assertThat(pagesFetched).isEqualTo(maxPages)
        assertThat(fetched).hasSize(totalItems)
        assertThat(fetched.map { it.balanceGame.gameDate }).containsExactlyElementsOf(expectedDatesDesc)
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 응답한 게임이 없으면 빈 결과를 반환한다.")
    @Test
    fun getBalanceGameHistory_WhenNoRespondedGamesReturnsEmpty() {
        // given
        val (myUser, _, couple) = setUpCouple()
        makeBalanceGame(3, LocalDate.of(2025, 5, 1)) // 게임은 존재하나 아무도 응답하지 않음
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).isEmpty()
        assertThat(result.cursor.next).isNull()
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 커플 중 한 명만 응답한 게임은 히스토리에서 제외된다.")
    @Test
    fun getBalanceGameHistory_ExcludesGamesAnsweredByOnlyOneMember() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val games = makeBalanceGame(2, LocalDate.of(2025, 5, 1)) // 05-01, 05-02
        // 05-01: 나만 응답, 05-02: 파트너만 응답 -> 둘 다 양쪽 응답이 아님
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[0].first, balanceGameOption = games[0].second.first(), user = myUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[1].first, balanceGameOption = games[1].second.first(), user = partnerUser)
        )
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).isEmpty()
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 커플 모두 응답한 게임은 my/partner 선택이 모두 매핑된다.")
    @Test
    fun getBalanceGameHistory_WhenBothRespondedBothChoicesMapped() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val (game, options) = makeBalanceGame(1, LocalDate.of(2025, 5, 1)).first()
        val myChoice = userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = game, balanceGameOption = options.last(), user = myUser)
        )
        val partnerChoice = userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = game, balanceGameOption = options.first(), user = partnerUser)
        )
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).hasSize(1)
        val choice = result.list.first().coupleChoiceOption
        assertThat(choice.myChoice!!.balanceGameOptionId).isEqualTo(myChoice.balanceGameOption.id)
        assertThat(choice.partnerChoice!!.balanceGameOptionId).isEqualTo(partnerChoice.balanceGameOption.id)
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 커플이 존재하지 않으면 빈 결과를 반환한다.")
    @Test
    fun getBalanceGameHistory_WhenCoupleNotExistsReturnsEmpty() {
        // given
        val (myUser, _, couple) = setUpCouple()
        makeBalanceGame(1, LocalDate.of(2025, 5, 1))
        val coupleRepository = mock<CoupleRepository>()
        whenever(coupleRepository.findByIdWithMembers(anyLong())).thenReturn(null)
        val balanceGameService =
            BalanceGameService(balanceGameRepository, userChoiceOptionRepository, coupleRepository, userRepository, mock<ApplicationEventPublisher>())
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).isEmpty()
        assertThat(result.cursor.next).isNull()
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 양쪽 응답 중 하나가 soft delete되면 해당 게임은 제외된다.")
    @Test
    fun getBalanceGameHistory_WhenOneOfBothChoicesSoftDeletedExcludesGame() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val games = makeBalanceGame(2, LocalDate.of(2025, 5, 1)) // 05-01, 05-02
        // 05-01: 양쪽 응답 (포함되어야 함)
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[0].first, balanceGameOption = games[0].second.first(), user = myUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[0].first, balanceGameOption = games[0].second.last(), user = partnerUser)
        )
        // 05-02: 양쪽 응답했지만 파트너 응답을 soft delete -> 활성 응답 1명 -> 제외
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[1].first, balanceGameOption = games[1].second.first(), user = myUser)
        )
        val deletedChoice = userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[1].first, balanceGameOption = games[1].second.last(), user = partnerUser)
        )
        deletedChoice.deleteEntity()
        userChoiceOptionRepository.save(deletedChoice)
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list).hasSize(1)
        assertThat(result.list.first().balanceGame.gameDate).isEqualTo(games[0].first.gameDate)
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 양쪽이 모두 응답한 게임만 gameDate 내림차순으로 내려온다.")
    @Test
    fun getBalanceGameHistory_IncludesOnlyGamesBothAnswered() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val games = makeBalanceGame(3, LocalDate.of(2025, 5, 1)) // 05-01, 05-02, 05-03
        // 05-01: 양쪽, 05-02: 나만, 05-03: 양쪽 -> 05-03, 05-01 만 노출
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[0].first, balanceGameOption = games[0].second.first(), user = myUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[0].first, balanceGameOption = games[0].second.last(), user = partnerUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[1].first, balanceGameOption = games[1].second.first(), user = myUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[2].first, balanceGameOption = games[2].second.first(), user = myUser)
        )
        userChoiceOptionRepository.save(
            UserChoiceOption(balanceGame = games[2].first, balanceGameOption = games[2].second.last(), user = partnerUser)
        )
        val queryVo = BalanceGameHistoryQueryVo(
            size = 10,
            cursor = null,
            sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
        )

        // when
        val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

        // then
        assertThat(result.list.map { it.balanceGame.gameDate }).containsExactly(
            LocalDate.of(2025, 5, 3),
            LocalDate.of(2025, 5, 1),
        )
    }

    @DisplayName("밸런스 게임 히스토리 조회 시 진행중인 오늘 게임은 응답했더라도 히스토리에서 제외된다.")
    @Test
    fun getBalanceGameHistory_ExcludesTodayGame() {
        // given
        val (myUser, partnerUser, couple) = setUpCouple()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val games = makeBalanceGame(2, LocalDate.of(2025, 5, 4)) // 05-04(어제), 05-05(오늘)
            games.forEach { (game, options) ->
                userChoiceOptionRepository.save(
                    UserChoiceOption(balanceGame = game, balanceGameOption = options.first(), user = myUser)
                )
                userChoiceOptionRepository.save(
                    UserChoiceOption(balanceGame = game, balanceGameOption = options.last(), user = partnerUser)
                )
            }
            val queryVo = BalanceGameHistoryQueryVo(
                size = 10,
                cursor = null,
                sortType = BalanceGameHistorySortType.GAME_DATE_DESC,
            )

            // when
            val result = balanceGameService.getBalanceGameHistory(myUser.id, couple.id, queryVo)

            // then
            assertThat(result.list).hasSize(1)
            assertThat(result.list.first().balanceGame.gameDate).isEqualTo(LocalDate.of(2025, 5, 4))
        }
    }

    private fun setUpCouple(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id",
    ): Triple<User, User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformId,
            partnerPlatformId
        )
        // 커플 멤버는 register 시 gender가 필수이므로 테스트에서도 gender를 부여한다.
        myUser.gender = UserGender.MALE
        partnerUser.gender = UserGender.FEMALE
        userRepository.save(myUser)
        userRepository.save(partnerUser)
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
