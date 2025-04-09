package com.whatever.domain.calendarevent.scheduleevent.service

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
import java.time.ZoneId
import kotlin.system.measureTimeMillis
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class ScheduleEventServiceTest @Autowired constructor(
    private val coupleRepository: CoupleRepository,
    private val userRepository: UserRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val contentRepository: ContentRepository
) {

    companion object {
        val NOW = DateTimeUtil.localNow()
    }

    @Autowired
    private lateinit var tagContentMappingRepository: TagContentMappingRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var scheduleEventService: ScheduleEventService

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

    @DisplayName("나의 Schedule 업데이트 시 request 값들이 정상적으로 반영된다.")
    @Test
    fun updateSchedule() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdOrNull(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(request.title)
            assertThat(content.contentDetail.description).isEqualTo(request.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(request.startTimeZone.toZonId())
            assertThat(startDateTime).isEqualTo(request.startDateTime.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.endTimeZone!!.toZonId())
            assertThat(endDateTime).isEqualTo(request.endDateTime!!.withoutNano)
        }
    }

    @DisplayName("같은 커플에 속한 상대방의 Schedule 업데이트 시 request 값들이 정상적으로 반영된다.")
    @Test
    fun updateSchedule_WithPartnerSchedule() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdOrNull(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(request.title)
            assertThat(content.contentDetail.description).isEqualTo(request.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(request.startTimeZone.toZonId())
            assertThat(startDateTime).isEqualTo(request.startDateTime.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.endTimeZone!!.toZonId())
            assertThat(endDateTime).isEqualTo(request.endDateTime!!.withoutNano)
        }
    }

    @DisplayName("Schedule 업데이트 시 endDate와 관련된 정보가 없다면 해당 일의 종료 시각으로 계산된다.")
    @Test
    fun updateSchedule_WithoutEndDateData() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdOrNull(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(endDateTime).isEqualTo(request.startDateTime.endOfDay.withoutNano)
            assertThat(endTimeZone).isEqualTo(request.startTimeZone.toZonId())
        }
    }

    @DisplayName("Schedule 업데이트 시 Title과 Descriptio이 없다면 예외가 발생한다.")
    @Test
    fun updateSchedule_WithoutTitleAndDescription() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

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

    @DisplayName("Schedule 업데이트 시 Title과 Description 둘 중 하나라도 Blank라면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource(
        "test title, '      '",
        "test title, ''",
        "'        ', test description",
        "''        , test description",
    )
    fun updateSchedule_WithBothBlankTitleAndDescription(title: String, description: String) {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

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
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

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
        val (ownerUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

        // when, then
        val exception = assertThrows<ScheduleIllegalStateException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                request = request
            )
        }
        assertThat(exception)
            .hasMessage(ScheduleExceptionCode.ILLEGAL_OWNER_STATUS.message)
    }

    @DisplayName("Schedule의 Content Tag 업데이트 시 Tag의 삭제와 추가가 모두 반영된다.")
    @Test
    fun updateSchedule_WithTags() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
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

        securityUtilMock.apply {
            whenever(SecurityUtil.getCurrentUserId()).thenReturn(myUser.id)
            whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
        }

        // when
        scheduleEventService.updateSchedule(
            scheduleId = oldSchedule.id,
            request = request
        )

        // then
        val updatedTagMappings = tagContentMappingRepository.findAllByContentId(oldContent.id)
        assertThat(updatedTagMappings).hasSize(newTags.size)

        val updatedTagLabels = updatedTagMappings.map { it.tag.label }
        val expectedTagLabels = newTags.map { it.label }
        assertThat(updatedTagLabels).containsExactlyInAnyOrderElementsOf(expectedTagLabels)
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
    myUser.setCouple(savedCouple)
    partnerUser.setCouple(savedCouple)

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