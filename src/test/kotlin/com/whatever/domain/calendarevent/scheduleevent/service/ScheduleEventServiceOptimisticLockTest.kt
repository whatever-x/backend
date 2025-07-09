package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalStateException
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.findByIdAndNotDeleted
import com.whatever.util.toZoneId
import com.whatever.util.withoutNano
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.ZoneId
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executors
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class ScheduleEventServiceOptimisticLockTest @Autowired constructor(
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val scheduleEventService: ScheduleEventService,
) {

    companion object {
        val NOW = DateTimeUtil.localNow()
    }

    @MockitoSpyBean
    private lateinit var scheduleEventRepository: ScheduleEventRepository

    private lateinit var securityUtilMock: AutoCloseable

    @BeforeEach
    fun setUp() {
        securityUtilMock = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        reset(scheduleEventRepository)

        securityUtilMock.close()
        tagContentMappingRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    private fun setUpCouple(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id"
    ): Triple<User, User, Couple> {
        return createCouple(userRepository, coupleRepository, myPlatformId, partnerPlatformId)
    }
    private fun setUpCoupleAndSecurity(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id"
    ): Triple<User, User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository, myPlatformId, partnerPlatformId)
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }
        return Triple(myUser, partnerUser, couple)
    }

    @DisplayName("Schedule 업데이트 시 request 값들이 정상적으로 반영된다.")
    @Test
    fun updateSchedule_WithOptimisticLock() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(7),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "updated title",
            description = "updated description",
            isCompleted = true,
            startDateTime = NOW.minusDays(2),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW,
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        val threadCount = 2
        val executor = Executors.newFixedThreadPool(threadCount)

        val futures = mutableListOf<CompletableFuture<Unit>>()
        for (i in 1..threadCount) {
            val future = CompletableFuture.supplyAsync({
                mockStatic(SecurityUtil::class.java).use {
                    it.apply {
                        whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
                        whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
                    }
                    scheduleEventService.updateSchedule(
                        scheduleId = oldSchedule.id,
                        request = request
                    )
                }
            }, executor)
            futures.add(future)
        }

        // when
        val completionException = assertThrows<CompletionException> {
            futures.forEach { it.join() }
        }
        val resultException = completionException.cause

        // then

        // persist가 늦은 요청 반려된다.
        assertThat(resultException).run {
            isInstanceOf(ScheduleIllegalStateException::class.java)
            hasMessage(ScheduleExceptionCode.UPDATE_CONFLICT.message)
        }

        // persist가 빠른 요청은 성공한다.
        val updatedScheduleEvent = scheduleEventRepository.findByIdWithContent(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(request.title)
            assertThat(content.contentDetail.description).isEqualTo(request.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(request.startTimeZone!!.toZoneId())
            assertThat(startDateTime).isEqualTo(request.startDateTime!!.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.endTimeZone!!.toZoneId())
            assertThat(endDateTime).isEqualTo(request.endDateTime!!.withoutNano)
        }
    }

     // TODO(준용) 항상 통과하지 않는 테스트. 안정화 필요
//    @DisplayName("Schedule 삭제 시 충돌이 발생해도 최대 3회까지 재시도 후 반영된다.")
//    @Test
//    fun deleteSchedule_WithOptimisticLock() {
//        fun makeFuture(
//            currentUser: User,
//            currentCouple: Couple,
//            serviceMethod: () -> Unit,
//            executor: ExecutorService,
//        ): CompletableFuture<Unit> {
//            return CompletableFuture.supplyAsync({
//                mockStatic(SecurityUtil::class.java).use {
//                    it.apply {
//                        whenever(SecurityUtil.getCurrentUserId()).thenReturn(currentUser.id)
//                        whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(currentCouple.id)
//                    }
//                    serviceMethod.invoke()
//                }
//            }, executor)
//        }
//
//        // given
//        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
//        val oldContent = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
//        val oldSchedule = scheduleEventRepository.save(
//            ScheduleEvent(
//                uid = "test-uuid4-value",
//                startDateTime = NOW.minusDays(7),
//                startTimeZone = ZoneId.of("Asia/Seoul"),
//                endDateTime = NOW.minusDays(3),
//                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
//                content = oldContent,
//            )
//        )
//        val request = UpdateScheduleRequest(
//            selectedDate = DateTimeUtil.localNow().toLocalDate(),
//            title = "updated title",
//            description = "updated description",
//            isCompleted = true,
//            startDateTime = NOW.minusDays(2),
//            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
//            endDateTime = NOW,
//            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
//        )
//
//        val threadCount = 2
//        val executor = Executors.newFixedThreadPool(threadCount)
//
//        val futures = mutableListOf<CompletableFuture<Unit>>()
//        futures.add(makeFuture(
//            currentUser = myUser,
//            currentCouple = couple,
//            serviceMethod = { scheduleEventService.updateSchedule(oldSchedule.id, request) },
//            executor = executor
//        ))
//        futures.add(makeFuture(
//            currentUser = myUser,
//            currentCouple = couple,
//            serviceMethod = { scheduleEventService.deleteSchedule(oldSchedule.id) },
//            executor = executor
//        ))
//
//        // when
//        futures.forEach { it.join() }
//
//        // then
//        val deletedSchedule = scheduleEventRepository.findByIdAndNotDeleted(oldSchedule.id)
//        assertThat(deletedSchedule).isNull()
//    }

    @DisplayName("Schedule 삭제 시 3번 초과로 시도하면 예외를 반환한다.")
    @Test
    fun deleteSchedule_WithOptimisticLockFail() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(7),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }
        whenever(scheduleEventRepository.findByIdWithContentAndUser(any()))
            .thenThrow(ObjectOptimisticLockingFailureException::class.java)

        // when
        val resultException = assertThrows<ScheduleIllegalStateException> {
            scheduleEventService.deleteSchedule(oldSchedule.id)
        }

        // then
        assertThat(resultException).run {
            isInstanceOf(ScheduleIllegalStateException::class.java)
            hasMessage(ScheduleExceptionCode.UPDATE_CONFLICT.message)
        }

        val unDeletedSchedule = scheduleEventRepository.findByIdAndNotDeleted(oldSchedule.id)
        assertThat(unDeletedSchedule).isNotNull
    }

}
