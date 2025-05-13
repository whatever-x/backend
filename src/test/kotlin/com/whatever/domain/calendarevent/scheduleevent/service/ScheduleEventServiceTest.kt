package com.whatever.domain.calendarevent.scheduleevent.service

import com.whatever.domain.calendarevent.controller.dto.request.GetCalendarQueryParameter
import com.whatever.domain.calendarevent.scheduleevent.controller.dto.UpdateScheduleRequest
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleAccessDeniedException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleExceptionCode
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalArgumentException
import com.whatever.domain.calendarevent.scheduleevent.exception.ScheduleIllegalStateException
import com.whatever.domain.calendarevent.scheduleevent.model.ScheduleEvent
import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.model.Content
import com.whatever.domain.content.model.ContentDetail
import com.whatever.domain.content.model.ContentType
import com.whatever.domain.content.repository.ContentRepository
import com.whatever.domain.content.tag.model.Tag
import com.whatever.domain.content.tag.model.TagContentMapping
import com.whatever.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.domain.content.tag.repository.TagRepository
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.couple.repository.CoupleRepository
import com.whatever.domain.user.model.LoginPlatform
import com.whatever.domain.user.model.User
import com.whatever.domain.user.model.UserStatus
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import com.whatever.util.endOfDay
import com.whatever.util.findByIdAndNotDeleted
import com.whatever.util.toDateTime
import com.whatever.util.toZonId
import com.whatever.util.withoutNano
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertNotNull

@ActiveProfiles("test")
@SpringBootTest
class ScheduleEventServiceTest @Autowired constructor(
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

    @DisplayName("나의 Schedule 업데이트 시 request 값들이 정상적으로 반영된다.")
    @Test
    fun updateSchedule() {
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

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdWithContent(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(request.title)
            assertThat(content.contentDetail.description).isEqualTo(request.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(request.startTimeZone!!.toZonId())
            assertThat(startDateTime).isEqualTo(request.startDateTime!!.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.endTimeZone!!.toZonId())
            assertThat(endDateTime).isEqualTo(request.endDateTime!!.withoutNano)
        }
    }

    @DisplayName("같은 커플에 속한 상대방의 Schedule 업데이트 시 request 값들이 정상적으로 반영된다.")
    @Test
    fun updateSchedule_WithPartnerSchedule() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(partnerUser, ContentType.SCHEDULE))
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

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdWithContent(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(request.title)
            assertThat(content.contentDetail.description).isEqualTo(request.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(request.startTimeZone!!.toZonId())
            assertThat(startDateTime).isEqualTo(request.startDateTime!!.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.endTimeZone!!.toZonId())
            assertThat(endDateTime).isEqualTo(request.endDateTime!!.withoutNano)
        }
    }

    @DisplayName("Schedule 업데이트 시 endDate와 관련된 정보가 없다면 해당 일의 종료 시각으로 계산된다.")
    @Test
    fun updateSchedule_WithoutEndDateData() {
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
        )

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdOrNull(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(endDateTime).isEqualTo(request.startDateTime!!.endOfDay.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.startTimeZone!!.toZonId())
        }
    }

    @DisplayName("Schedule 업데이트 시 Title과 Descriptio이 없다면 예외가 발생한다.")
    @Test
    fun updateSchedule_WithoutTitleAndDescription() {
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
            title = null,
            description = null,
            isCompleted = true,
            startDateTime = NOW.minusDays(2),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL.message)
    }

    @DisplayName("Schedule 업데이트 시 Title과 Description 둘 중 하나라도 Blank거나, 모두 nul이라면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource(
        "test title, '      '",
        "test title, ''",
        "'        ', test description",
        "''        , test description",
        "          ,                 ",
    )
    fun updateSchedule_WithBlankOrNullTitleAndDescription(title: String?, description: String?) {
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
            title = title,
            description = description,
            isCompleted = true,
            startDateTime = NOW.minusDays(2),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.ILLEGAL_CONTENT_DETAIL.message)
    }

    @DisplayName("Schedule 업데이트 시 endDateTime이 startDateTime보다 이르다면 예외가 발생한다.")
    @Test
    fun updateSchedule_WithInvalidDuration() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(5),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW,
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            endDateTime = NOW.minusDays(1),  // 유효하지 않은 endDateTime.
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.ILLEGAL_DURATION.message)
    }

    @DisplayName("Schedule 업데이트 시 현재 사용자의 커플과 작성자의 커플이 다른 경우 예외가 발생한다.")
    @Test
    fun updateSchedule_WithDifferentCouple() {
        // given
        val (ownerUser, partnerUser, couple) = setUpCouple()
        val oldContent = contentRepository.save(createContent(ownerUser, ContentType.SCHEDULE))
        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-diff-couple",
                startDateTime = NOW.minusDays(1),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW,
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )

        val (otherUser, otherUser2, otherCouple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformUserId = "other-user-platform-id",
            partnerPlatformUserId = "other-user-platform-id2",
        )
        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW.plusDays(1),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(otherUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(otherCouple.id)
        }

        // when, then
        val exception = assertThrows<ScheduleAccessDeniedException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.COUPLE_NOT_MATCHED.message)
    }

    @DisplayName("같은 커플에 속한 상대방의 Schedule 업데이트 시 작성자가 SINGLE 상태일 경우 예외가 발생한다.")
    @Test
    fun updateSchedule_WhenOwnerHasNoCouple() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(partnerUser, ContentType.SCHEDULE))
        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(5),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )

        partnerUser.userStatus = UserStatus.SINGLE  // 작성자가 모종의 이유로 SINGLE 상태로 전환
        userRepository.save(partnerUser)

        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW.plusDays(1),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleIllegalStateException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.ILLEGAL_PARTNER_STATUS.message)
    }

    @DisplayName("Schedule의 Content Tag 업데이트 시 Tag의 삭제와 추가가 모두 반영된다.")
    @Test
    fun updateSchedule_WithTags() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val oldContent = contentRepository.save(createContent(partnerUser, ContentType.SCHEDULE))
        val tagCount = 20
        val tagNamesSet = (1..tagCount).map { "testTag${it}" }.toSet()
        val tags = createTags(tagNamesSet, tagRepository)

        val oldTags = tags.filter { it.id <= 10 }.toSet()  // 1~10 태그를 Content에 할당
        addTags(oldContent, oldTags, tagContentMappingRepository)

        val oldSchedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(5),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = oldContent,
            )
        )

        val newTags = tags.filter { it.id in 6..tagCount }.toSet()  // 6~15 태그로 변경
        val newTagIds = newTags.map { it.id }.toSet()

        val request = UpdateScheduleRequest(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW.plusDays(1),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
            tagIds = newTagIds
        )

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedTagMappings = tagContentMappingRepository.findAllByContentIdWithTag(oldContent.id)
        assertThat(updatedTagMappings).hasSize(newTags.size)

        val updatedTagLabels = updatedTagMappings.map { it.tag.label }
        val expectedTagLabels = newTags.map { it.label }
        assertThat(updatedTagLabels).containsExactlyInAnyOrderElementsOf(expectedTagLabels)
    }

    @DisplayName("나의 Schedule 업데이트 시 StartDateTime이 null이라면 Content는 Memo로 복구된다.")
    @Test
    fun updateSchedule_WithoutStartDateTime() {
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
            startDateTime = null,  // Content를 Memo로 복구하기 위해 nul로 설정
            startTimeZone = null,
            endDateTime = NOW,
            endTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val deletedSchedule = scheduleEventRepository.findByIdAndNotDeleted(oldSchedule.id)
        assertThat(deletedSchedule).isNull()
        val content = contentRepository.findByIdAndNotDeleted(oldContent.id)
        assertNotNull(content)
        assertThat(content.type).isEqualTo(ContentType.MEMO)
        assertThat(content.contentDetail.title).isEqualTo(request.title)
        assertThat(content.contentDetail.description).isEqualTo(request.description)
    }

    @DisplayName("내가 업로드한 스케줄을 삭제하면 연관된 데이터들이 모두 삭제된다.")
    @Test
    fun deleteSchedule() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val content = contentRepository.save(createContent(partnerUser, ContentType.SCHEDULE))
        val tagCount = 20
        val tagNamesSet = (1..tagCount).map { "testTag${it}" }.toSet()
        val tags = createTags(tagNamesSet, tagRepository)
        addTags(content, tags, tagContentMappingRepository)
        val schedule = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uuid4-value",
                startDateTime = NOW.minusDays(5),
                startTimeZone = ZoneId.of("Asia/Seoul"),
                endDateTime = NOW.minusDays(3),
                endTimeZone = DateTimeUtil.UTC_ZONE_ID,
                content = content,
            )
        )

        // when
        scheduleEventService.deleteSchedule(schedule.id)

        // then
        val deletedSchedule = scheduleEventRepository.findByIdAndNotDeleted(schedule.id)
        val deletedContent = contentRepository.findByIdAndNotDeleted(content.id)
        val deletedTagContentMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(content.id)
        assertThat(deletedSchedule).isNull()
        assertThat(deletedContent).isNull()
        assertThat(deletedTagContentMappings).isEmpty()
    }

    @DisplayName("스케줄 조회 시 커플의 삭제되지 않은 스케줄이 모두 조회된다.")
    @Test
    fun getSchedule() {
        // given
        val startDate = LocalDate.of(2025, 4, 1)
        val endDate = LocalDate.of(2025, 4, 30)
        val userTimeZone = ZoneId.of("Asia/Seoul")

        fun createScheduleEvents(
            numberOfEvents: Int,
            uidPrefix: String,
            ownerSelector: (Int) -> User,
            startDate: LocalDate,
            timeZone: ZoneId,
        ): List<ScheduleEvent> {
            val scheduleEvents = mutableListOf<ScheduleEvent>()
            repeat(numberOfEvents) { idx ->
                val content = contentRepository.save(createContent(ownerSelector(idx), ContentType.SCHEDULE))
                val eventStart = startDate.plusDays((idx % 30L))
                val eventEnd = eventStart.plusDays(5)
                val event = scheduleEventRepository.save(
                    ScheduleEvent(
                        uid = "$uidPrefix$idx",
                        startDateTime = eventStart.toDateTime(),
                        startTimeZone = timeZone,
                        endDateTime = eventEnd.toDateTime(),
                        endTimeZone = timeZone,
                        content = content
                    )
                )
                scheduleEvents.add(event)
            }
            return scheduleEvents
        }

        // 조회 대상 커플 데이터 생성
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val numberOfEvents = 100
        val scheduleEvents = createScheduleEvents(
            numberOfEvents = numberOfEvents,
            uidPrefix = "test-uuid-",
            ownerSelector = { idx -> if (idx % 2 == 0) myUser else partnerUser },
            startDate = startDate,
            timeZone = userTimeZone
        )

        // 조회 대상의 삭제된 커플 데이터 생성
        createScheduleEvents(
            numberOfEvents = numberOfEvents,
            uidPrefix = "test-uuid-",
            ownerSelector = { idx -> if (idx % 2 == 0) myUser else partnerUser },
            startDate = startDate,
            timeZone = userTimeZone
        ).forEach { scheduleEventService.deleteSchedule(it.id) }

        // 조회 대상이 아닌, 다른 커플 데이터 생성
        val (otherUser, otherPartner, otherCouple) = setUpCouple(
            myPlatformId = "other-user-id",
            partnerPlatformId = "other-partner-id"
        )
        createScheduleEvents(
            numberOfEvents = 100,
            uidPrefix = "other-test-uuid-",
            ownerSelector = { idx -> if (idx % 2 == 0) otherUser else otherPartner },
            startDate = startDate,
            timeZone = userTimeZone
        )

        val request = GetCalendarQueryParameter(
            startDate = startDate,
            endDate = endDate,
            userTimeZone = userTimeZone.id
        )

        // when
        val result = scheduleEventService.getSchedule(
            startDate = request.startDate,
            endDate = request.endDate,
            userTimeZone = request.userTimeZone
        )

        // then
        assertThat(result).hasSize(numberOfEvents)

        val resultScheduleIds = result.map { it.scheduleId }
        val savedScheduleIds = scheduleEvents.map { it.id }
        assertThat(resultScheduleIds).containsExactlyInAnyOrderElementsOf(savedScheduleIds)
    }

    @DisplayName("스케줄 조회 시 날짜 범위에 맞는 일정들이 조회된다.")
    @Test
    fun getSchedule_WithDailyAndWeeklyRequest() {
        // given
        val startDate = LocalDate.of(2025, 4, 1)
        val userTimeZone = ZoneId.of("Asia/Seoul")

        // 조회 대상 커플 데이터 생성
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val numberOfEvents = 30
        val scheduleEvents = mutableListOf<ScheduleEvent>()
        repeat(numberOfEvents) { idx ->
            val content = contentRepository.save(createContent(if (idx % 2 == 0) myUser else partnerUser, ContentType.SCHEDULE))
            val eventStartDate = startDate.plusDays(idx.toLong())
            val event = scheduleEventRepository.save(
                ScheduleEvent(
                    uid = "test-uid-${idx}",
                    startDateTime = eventStartDate.toDateTime(),
                    startTimeZone = userTimeZone,
                    endDateTime = eventStartDate.toDateTime().endOfDay,
                    endTimeZone = userTimeZone,
                    content = content
                )
            )
            scheduleEvents.add(event)
        }

        val requestDaily = GetCalendarQueryParameter(
            startDate = startDate,
            endDate = startDate,
            userTimeZone = userTimeZone.id
        )
        val requestWeekly = GetCalendarQueryParameter(
            startDate = startDate,
            endDate = startDate.plusDays(6),
            userTimeZone = userTimeZone.id
        )

        // when
        val resultDaily = scheduleEventService.getSchedule(
            startDate = requestDaily.startDate,
            endDate = requestDaily.endDate,
            userTimeZone = requestDaily.userTimeZone
        )
        val resultWeekly = scheduleEventService.getSchedule(
            startDate = requestWeekly.startDate,
            endDate = requestWeekly.endDate,
            userTimeZone = requestWeekly.userTimeZone
        )

        // then
        assertThat(resultDaily).hasSize(1)
        assertThat(resultWeekly).hasSize(7)
    }
}

internal fun createTags(
    tagNames: Set<String>,
    tagRepository: TagRepository
): Set<Tag> {
    val tagSet = mutableSetOf<Tag>()
    for (name in tagNames) {
        tagSet.add(Tag(label = name))
    }
    return tagRepository.saveAll(tagSet).toSet()
}
internal fun addTags(
    content: Content,
    tags: Set<Tag>,
    tagContentMappingRepository: TagContentMappingRepository,
) {
    val tagContentMappings = tags.map { tag -> TagContentMapping(tag = tag, content = content) }
    tagContentMappingRepository.saveAll(tagContentMappings)
}

internal fun createCouple(
    userRepository: UserRepository,
    coupleRepository: CoupleRepository,
    myPlatformUserId: String = "my-user-id",
    partnerPlatformUserId: String = "partner-user-id",
): Triple<User, User, Couple> {
    val myUser = userRepository.save(createUser("my", myPlatformUserId))
    val partnerUser = userRepository.save(createUser("partner", partnerPlatformUserId))

    val startDate = DateTimeUtil.localNow().toLocalDate()
    val sharedMessage = "test message"
    val savedCouple = coupleRepository.save(
        Couple(
            startDate = startDate,
            sharedMessage = sharedMessage
        )
    )
    savedCouple.addMembers(myUser, partnerUser)

    userRepository.save(myUser)
    userRepository.save(partnerUser)
    return Triple(myUser, partnerUser, savedCouple)
}

internal fun createUser(
    nickname: String,
    platformUserId: String,
    userStatus: UserStatus = UserStatus.SINGLE
): User {
    return User(
        nickname = nickname,
        birthDate = DateTimeUtil.localNow().toLocalDate(),
        platform = LoginPlatform.KAKAO,
        platformUserId = platformUserId,
        userStatus = userStatus
    )
}

internal fun createContent(user: User, type: ContentType): Content {
    return Content(
        user = user,
        contentDetail = ContentDetail(
            title = "Default Title",
            description = "Default Description",
            isCompleted = false
        ),
        type = type
    )
}