package com.whatever.caramel.api.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.caramel.api.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.domain.firebase.service.FirebaseService
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.util.findByIdAndNotDeleted
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockReset.AFTER
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class ScheduleEventServiceCreateTest @Autowired constructor(
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val contentRepository: ContentRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val tagRepository: TagRepository,
    private val scheduleEventService: ScheduleEventService,
) : ExcludeAsyncConfigBean() {

    companion object {
        val NOW = DateTimeUtil.localNow()
    }

    private lateinit var securityUtilMock: AutoCloseable

    @MockitoBean(reset = AFTER)
    private lateinit var firebaseService: FirebaseService

    @BeforeEach
    fun setUp() {
        securityUtilMock = mockStatic(SecurityUtil::class.java)
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()
        tagContentMappingRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    private fun setUpCoupleAndSecurity(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id",
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

    @DisplayName("Schedule 생성에 성공하고, partnerUser에게 fcm 알림을 전송한다.")
    @Test
    fun createSchedule() {
        // given
        val (myUser, partnerUser, _) = setUpCoupleAndSecurity()

        val request = CreateScheduleRequest(
            title = "Schedule Title",
            description = "Description Content",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW.plusDays(2),
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when
        val result = scheduleEventService.createSchedule(request)

        // then
        val scheduleEvent = scheduleEventRepository.findByIdAndNotDeleted(result.contentId)
        require(scheduleEvent != null)
        assertThat(scheduleEvent.id).isEqualTo(result.contentId)

        val content = contentRepository.findByIdOrNull(scheduleEvent.content.id)
        require(content != null)
        assertThat(content.type).isEqualTo(ContentType.SCHEDULE)

        verify(firebaseService, only()).sendNotification(
            targetUserIds = eq(setOf(partnerUser.id)),
            fcmNotification = any(),
        )
    }

    @DisplayName("Schedule 생성 시 title과 description이 모두 null 또는 blank이면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource(
        "test title, '      '",
        "test title, ''",
        "'        ', test description",
        "''        , test description",
        "          ,                 ",
    )
    fun createSchedule_WithBlankOrNullTitleDescription(title: String?, description: String?) {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val request = CreateScheduleRequest(
            title = title,
            description = description,
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(request)
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL.message)
    }

    @DisplayName("endDateTime이 startDateTime보다 이르면 예외가 발생한다.")
    @Test
    fun createSchedule_WithInvalidDuration() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val request = CreateScheduleRequest(
            title = "title",
            description = "desc",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW.minusDays(1),
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(request)
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_DURATION.message)
    }
}
