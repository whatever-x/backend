package com.whatever.domain.balancegame.service

import com.whatever.domain.balancegame.exception.BalanceGameExceptionCode
import com.whatever.domain.balancegame.exception.BalanceGameIllegalStateException
import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.balancegame.repository.BalanceGameOptionRepository
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.calendarevent.scheduleevent.service.createCouple
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class BalanceGameServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val coupleRepository: CoupleRepository,
    private val balanceGameService: BalanceGameService,
    private val balanceGameRepository: BalanceGameRepository,
    private val balanceGameOptionRepository: BalanceGameOptionRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
) {

    private lateinit var securityUtilMock: AutoCloseable

    @BeforeEach
    fun setUp() {
        securityUtilMock = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()
        userChoiceOptionRepository.deleteAllInBatch()
        balanceGameOptionRepository.deleteAllInBatch()
        balanceGameRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("밸러스 게임을 조회 시 커플 멤버중 아무도 선택하지 않았을 경우 게임의 정보만 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WithNoMemberChoices() {
        // given
        setUpCoupleAndSecurity()
        val now = LocalDateTime.of(2025, 5, 5, 9, 0)
        mockStatic(DateTimeUtil::class.java).use {
            whenever(DateTimeUtil.localNow(any())).thenReturn(now)
            val expectedGame = makeBalanceGame(1, now.toLocalDate()).first()

            // when
            val result = balanceGameService.getTodayBalanceGameInfo()

            // then
            assertThat(result.gameInfo.id).isEqualTo(expectedGame.first.id)
            assertThat(result.options.map { it.id }).containsExactlyInAnyOrderElementsOf(expectedGame.second.map { it.id })
            assertThat(result.myChoice).isNull()
            assertThat(result.partnerChoice).isNull()
        }
    }

    @DisplayName("밸러스 게임을 조회 시 커플 멤버중 나만 선택했을 경우 내 선택 정보도 함께 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WithIHaveChosen() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
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
            val result = balanceGameService.getTodayBalanceGameInfo()

            // then
            assertThat(result.myChoice?.userId).isEqualTo(myChoice.user.id)
            assertThat(result.myChoice?.optionId).isEqualTo(myChoice.balanceGameOption.id)
            assertThat(result.partnerChoice).isNull()
        }
    }

    @DisplayName("밸러스 게임을 조회 시 커플 멤버중 파트너만 선택했을 경우 파트너 선택 정보도 함께 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WhenPartnerChosen() {
        // given
        val (_, partnerUser, _) = setUpCoupleAndSecurity()
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
            val result = balanceGameService.getTodayBalanceGameInfo()

            // then
            assertThat(result.myChoice).isNull()
            assertThat(result.partnerChoice?.userId).isEqualTo(partnerChoice.user.id)
            assertThat(result.partnerChoice?.optionId).isEqualTo(partnerChoice.balanceGameOption.id)
        }
    }

    @DisplayName("밸러스 게임을 조회 시 커플멤버 모두 선택했을 경우 선택 정보도 함께 반환된다.")
    @Test
    fun getTodayBalanceGameInfo_WhenBothMembersChosen() {
        // given
        val (myUser, partnerUser, _) = setUpCoupleAndSecurity()
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
            val result = balanceGameService.getTodayBalanceGameInfo()

            // then
            assertThat(result.myChoice?.userId).isEqualTo(myChoice.user.id)
            assertThat(result.myChoice?.optionId).isEqualTo(myChoice.balanceGameOption.id)
            assertThat(result.partnerChoice?.userId).isEqualTo(partnerChoice.user.id)
            assertThat(result.partnerChoice?.optionId).isEqualTo(partnerChoice.balanceGameOption.id)
        }
    }

    @DisplayName("밸러스 게임을 조회 시 선택지가 두개 미만일 경우 예외가 발생한다.")
    @Test
    fun getTodayBalanceGameInfo_WithIllegalOptionCount() {
        // given
        val (_, _, _) = setUpCoupleAndSecurity()
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

    private fun setUpCoupleAndSecurity(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id"
    ): Triple<User, User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformId,
            partnerPlatformId
        )
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }
        return Triple(myUser, partnerUser, couple)
    }

    /**
     * @param count 시작일로부터 count개수 만큼의 게임을 만듭니다.
     * @param startGameDate 게임을 생성할 시작일을 지정합니다.
     */
    private fun makeBalanceGame(count: Int, startGameDate: LocalDate): ArrayList<Pair<BalanceGame, List<BalanceGameOption>>> {
        val gameList = arrayListOf<Pair<BalanceGame, List<BalanceGameOption>>>()
        for (i in 0..(count-1)) {
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