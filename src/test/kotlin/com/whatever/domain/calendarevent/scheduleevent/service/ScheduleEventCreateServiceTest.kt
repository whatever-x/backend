package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.scheduleevent.controller.dto.CreateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleAccessDeniedException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
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
) {

    companion object {
        val NOW = DateTimeUtil.localNow()
    }

    private lateinit var securityUtilMock: AutoCloseable

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

    @DisplayName("MEMO 타입 컨텐츠에서 정상적으로 Schedule 생성에 성공한다.")
    @Test
    fun createSchedule() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))

        val request = CreateScheduleRequest(
            contentId = memo.id,
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
        val content = contentRepository.findByIdAndNotDeleted(memo.id)
        require(scheduleEvent != null)
        require(content != null)
        assertThat(scheduleEvent.content.id).isEqualTo(memo.id)
        assertThat(content.type).isEqualTo(ContentType.SCHEDULE)
        assertThat(content.contentDetail.title).isEqualTo(request.title)
        assertThat(content.contentDetail.description).isEqualTo(request.description)
    }

    @DisplayName("존재하지 않거나 삭제된 contentId 입력 시 예외가 발생한다.")
    @Test
    fun createSchedule_WithIllegalContentId() {
        // given
        setUpCoupleAndSecurity()
        val request = CreateScheduleRequest(
            contentId = 0L, // 존재하지 않는 ID
            title = "title",
            description = "desc",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(request)
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_ID.message)
    }

    @DisplayName("MEMO 타입이 아닌 컨텐츠에서 생성 요청 시 예외가 발생한다.")
    @Test
    fun createSchedule_WithNonMemoContent() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val nonMemo = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val request = CreateScheduleRequest(
            contentId = nonMemo.id,
            title = "title",
            description = "desc",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.createSchedule(request)
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_ID.message)
    }

    @DisplayName("다른 커플의 메모를 Schedule로 만들 경우 예외가 발생한다.")
    @Test
    fun createSchedule_WithOtherCoupleMemo() {
        // given
        val (ownerUser, _, _) = createCouple(userRepository, coupleRepository, "owner", "partner-owner")
        val memo = contentRepository.save(createContent(ownerUser, ContentType.MEMO))
        val (otherUser, _, otherCouple) = createCouple(userRepository, coupleRepository, "other", "other2")
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(otherUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(otherCouple.id)
        }
        val request = CreateScheduleRequest(
            contentId = memo.id,
            title = "title",
            description = "desc",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id
        )
        // when, then
        val exception = assertThrows<ScheduleAccessDeniedException> {
            scheduleEventService.createSchedule(request)
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.COUPLE_NOT_MATCHED.message)
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
            contentId = memo.id,
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

    @DisplayName("endDateTime이 startDateTime보다 이르면 예외 발생")
    @Test
    fun createSchedule_WithInvalidDuration() {
        // given
        val (myUser, _, _) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val request = CreateScheduleRequest(
            contentId = memo.id,
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
