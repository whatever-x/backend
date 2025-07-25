package com.whatever.caramel.domain.calendarevent.scheduleevent.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.service.ScheduleEventService
import com.whatever.caramel.domain.calendarevent.vo.CreateScheduleVo
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.caramel.domain.findByIdAndNotDeleted
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.only
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockReset.AFTER
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.Test

@CaramelDomainSpringBootTest
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

    @MockitoBean(reset = AFTER)
    private lateinit var firebaseService: FirebaseService

    @AfterEach
    fun tearDown() {
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
        return Triple(myUser, partnerUser, couple)
    }

    @DisplayName("Schedule 생성에 성공하고, partnerUser에게 fcm 알림을 전송한다.")
    @Test
    fun createSchedule() {
        // given
        val (myUser, partnerUser, _) = setUpCoupleAndSecurity()

        val createScheduleVo = CreateScheduleVo(
            title = "Schedule Title",
            description = "Description Content",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW.plusDays(2),
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            contentAsignee = ContentAssignee.ME,
        )

        // when
        val contentSummaryResult = scheduleEventService.createSchedule(
            scheduleVo = createScheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다")
        )

        // then
        val scheduleEvent = scheduleEventRepository.findByIdAndNotDeleted(contentSummaryResult.id)
        require(scheduleEvent != null)
        assertThat(scheduleEvent.id).isEqualTo(contentSummaryResult.id)

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
        val createScheduleVo = CreateScheduleVo(
            title = title,
            description = description,
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            contentAsignee = ContentAssignee.ME,
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(
                scheduleVo = createScheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다")
            )
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL.message)
    }

    @DisplayName("endDateTime이 startDateTime보다 이르면 예외가 발생한다.")
    @Test
    fun createSchedule_WithInvalidDuration() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val createScheduleVo = CreateScheduleVo(
            title = "title",
            description = "desc",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW.minusDays(1),
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            contentAsignee = ContentAssignee.ME,
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(
                scheduleVo = createScheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다")
            )
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_DURATION.message)
    }
}
