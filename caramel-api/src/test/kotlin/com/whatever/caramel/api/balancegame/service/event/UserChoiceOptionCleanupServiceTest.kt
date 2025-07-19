package com.whatever.caramel.api.balancegame.service.event

import com.whatever.domain.balancegame.model.BalanceGame
import com.whatever.domain.balancegame.model.BalanceGameOption
import com.whatever.domain.balancegame.model.UserChoiceOption
import com.whatever.domain.balancegame.repository.BalanceGameOptionRepository
import com.whatever.domain.balancegame.repository.BalanceGameRepository
import com.whatever.domain.balancegame.repository.UserChoiceOptionRepository
import com.whatever.domain.calendarevent.scheduleevent.service.event.ScheduleEventListener
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.caramel.api.couple.service.makeCouple
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.caramel.common.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class UserChoiceOptionCleanupServiceTest @Autowired constructor(
    private val balanceGameRepository: BalanceGameRepository,
    private val balanceGameOptionRepository: BalanceGameOptionRepository,
    private val userChoiceOptionRepository: UserChoiceOptionRepository,
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
) {

    @Autowired
    private lateinit var userChoiceOptionCleanupService: UserChoiceOptionCleanupService

    @AfterEach
    fun tearDown() {
        userChoiceOptionRepository.deleteAllInBatch()
        balanceGameOptionRepository.deleteAllInBatch()
        balanceGameRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    @DisplayName("UserChoiceOptionCleanupService.cleanupEntity() 호출 시 특정 사용자의 선택들이 모두 제거된다.")
    @Test
    fun cleanupSchedule() {
        // given
        val (myUser, partnerUser, _) = makeCouple(userRepository, coupleRepository)

        val gameSize = 50
        val optionsPerGame = 2
        val gameWithOptionsList = createBalanceGamesWithOptions(
            balanceGameRepository = balanceGameRepository,
            balanceGameOptionRepository = balanceGameOptionRepository,
            gamesCount = gameSize,
            optionsPerGame = optionsPerGame
        )
        val myChoices = createUserChoiceOption(userChoiceOptionRepository, gameWithOptionsList, myUser)
        val partnerChoices = createUserChoiceOption(userChoiceOptionRepository, gameWithOptionsList, partnerUser)

        // when
        val deletedEntityCnt = userChoiceOptionCleanupService.cleanupEntity(
            userId = myUser.id,
            entityName = ScheduleEventListener.ENTITY_NAME
        )

        // then
        assertThat(deletedEntityCnt).isEqualTo(myChoices.size)

        val remainingChoiceIds = userChoiceOptionRepository.findAll().filter { !it.isDeleted }.map { it.id }
        assertThat(remainingChoiceIds).containsExactlyInAnyOrderElementsOf(partnerChoices.map { it.id })
    }
}

fun createUserChoiceOption(
    userChoiceOptionRepository: UserChoiceOptionRepository,
    gameWithOptionsList: List<BalanceGame>,
    user: User,
): List<UserChoiceOption> {
    val userChoiceOptions = gameWithOptionsList.map {
        UserChoiceOption(
            balanceGame = it,
            balanceGameOption = it.options.first(),
            user = user,
        )
    }
    return userChoiceOptionRepository.saveAll(userChoiceOptions)
}

fun createBalanceGames(
    balanceGameRepository: BalanceGameRepository,
    count: Int,
): List<BalanceGame> {
    if (count <= 0) return emptyList()

    val today = DateTimeUtil.localNow().toLocalDate()
    val gamesToSave = (1..count).map { i ->
        BalanceGame(
            gameDate = today.minusDays(i.toLong()),
            question = "테스트 질문 $i"
        )
    }
    return balanceGameRepository.saveAll(gamesToSave)
}

fun createBalanceGameOptions(
    balanceGameOptionRepository: BalanceGameOptionRepository,
    balanceGame: BalanceGame,
    count: Int,
): List<BalanceGameOption> {
    if (count <= 0) return emptyList()

    val optionsToSave = (1..count).map { i ->
        BalanceGameOption(
            optionText = "테스트 옵션 $i",
            balanceGame = balanceGame
        )
    }
    return balanceGameOptionRepository.saveAll(optionsToSave)
}

fun createBalanceGamesWithOptions(
    balanceGameRepository: BalanceGameRepository,
    balanceGameOptionRepository: BalanceGameOptionRepository,
    gamesCount: Int,
    optionsPerGame: Int,
): List<BalanceGame> {
    val games = createBalanceGames(balanceGameRepository, gamesCount)
    return games.map {
        BalanceGame(
            id = it.id,
            gameDate = it.gameDate,
            question = it.question,
            options = createBalanceGameOptions(balanceGameOptionRepository, it, optionsPerGame)
        )
    }
}
