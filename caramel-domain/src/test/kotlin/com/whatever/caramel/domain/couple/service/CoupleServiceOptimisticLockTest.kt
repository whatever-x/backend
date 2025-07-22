package com.whatever.caramel.domain.couple.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode
import com.whatever.caramel.domain.couple.exception.CoupleIllegalStateException
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger { }

@CaramelDomainSpringBootTest
class CoupleServiceOptimisticLockTest @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>,
    private val coupleService: CoupleService,
    private val userRepository: UserRepository,
) {

    @MockitoSpyBean
    private lateinit var coupleRepository: CoupleRepository

    @BeforeEach
    fun setUp() {
        val connectionFactory = redisTemplate.connectionFactory
        check(connectionFactory != null)
        connectionFactory.connection.serverCommands().flushAll()
        userRepository.deleteAllInBatch()
    }

    @AfterEach
    fun tearDown() {
        reset(coupleRepository)
    }

    @DisplayName("커플 시작일 업데이트 요청이 동시에 들어올 경우 요청이 모두 반영된다.")
    @Test
    fun updateStartDate_WithOptimisticLock() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        val members = listOf(myUser, partnerUser)
        val threadCount = 2
        val executor = Executors.newFixedThreadPool(threadCount)
        val startDates = List(threadCount) { idx ->
            LocalDate.EPOCH.plusDays(idx.toLong())
        }
        val requests = startDates
        val timeZone = "Asia/Seoul"

        val futures = requests.mapIndexed { idx, request ->
            CompletableFuture.supplyAsync({
                coupleService.updateStartDate(savedCouple.id, request, timeZone)
            }, executor)
        }

        // when
        val results = futures.map { it.join() }

        // then
        results.forEachIndexed { idx, response ->
            assertThat(response.id).isEqualTo(savedCouple.id)
            assertThat(response.startDate).isEqualTo(requests[idx])
        }
    }

    @Order(1)
    @DisplayName("커플 시작일 업데이트가 3번 초과로 실행될 경우 예외를 반환한다.")
    @Test
    fun updateStartDate_WithOptimisticLockFail() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        whenever(coupleRepository.findByIdOrNull(any())).doThrow(ObjectOptimisticLockingFailureException::class)

        val request = LocalDate.EPOCH
        val timeZone = "Asia/Seoul"

        // when
        val exception = assertThrows<CoupleIllegalStateException> {
            coupleService.updateStartDate(savedCouple.id, request, timeZone)
        }

        // then
        assertThat(exception)
            .isInstanceOf(CoupleIllegalStateException::class.java)
            .hasMessage(CoupleExceptionCode.UPDATE_FAIL.message)
    }

    @DisplayName("커플 공유메시지 업데이트 요청이 동시에 들어올 경우 요청이 모두 반영된다.")
    @Test
    fun updateSharedMessage_WithOptimisticLock() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        val members = listOf(myUser, partnerUser)
        val threadCount = 2
        val executor = Executors.newFixedThreadPool(threadCount)
        val sharedMessages = List(threadCount) { idx ->
            "updated sharedMessage: $idx"
        }
        val requests = sharedMessages

        val futures = requests.mapIndexed { idx, request ->
            CompletableFuture.supplyAsync({
                coupleService.updateSharedMessage(savedCouple.id, request)
            }, executor)
        }

        // when
        val results = futures.map { it.join() }

        // then
        results.forEachIndexed { idx, response ->
            assertThat(response.id).isEqualTo(savedCouple.id)
            assertThat(response.sharedMessage).isEqualTo(requests[idx])
        }
    }

    @DisplayName("커플 시작일, 공유메시지 업데이트 요청이 동시에 들어올 경우 요청이 유실되지 않고 모두 반환된다.")
    @Test
    fun updateStartDate_WithUpdateSharedMessageOptimisticLock() {
        // given
        val (myUser, partnerUser, savedCouple) = makeCouple(userRepository, coupleRepository)

        val threadCount = 2
        val executor = Executors.newFixedThreadPool(threadCount)

        val updateStartDateRequest = LocalDate.EPOCH
        val updateSharedMessageRequest = "updated sharedMessage"
        val timeZone = "Asia/Seoul"

        val futures = listOf(
            CompletableFuture.supplyAsync({
                coupleService.updateStartDate(savedCouple.id, updateStartDateRequest, timeZone)
            }, executor),
            CompletableFuture.supplyAsync({
                coupleService.updateSharedMessage(savedCouple.id, updateSharedMessageRequest)
            }, executor)
        )

        // when
        val results = futures.map { it.join() }

        // then
        assertThat(results[0].id).isEqualTo(savedCouple.id)
        assertThat(results[0].startDate).isEqualTo(updateStartDateRequest)

        assertThat(results[1].id).isEqualTo(savedCouple.id)
        assertThat(results[1].sharedMessage).isEqualTo(updateSharedMessageRequest)

        val updatedCouple = coupleRepository.findByIdOrNull(savedCouple.id)!!
        assertThat(updatedCouple.startDate).isEqualTo(updateStartDateRequest)
        assertThat(updatedCouple.sharedMessage).isEqualTo(updateSharedMessageRequest)
    }
}
