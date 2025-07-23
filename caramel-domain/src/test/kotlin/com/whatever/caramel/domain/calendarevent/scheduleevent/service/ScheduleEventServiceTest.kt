package com.whatever.caramel.domain.calendarevent.scheduleevent.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.endOfDay
import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.common.util.toZoneId
import com.whatever.caramel.common.util.withoutNano
import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.calendarevent.exception.ScheduleAccessDeniedException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
import com.whatever.caramel.domain.calendarevent.exception.ScheduleNotFoundException
import com.whatever.caramel.domain.calendarevent.model.ScheduleEvent
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.service.ScheduleEventService
import com.whatever.caramel.domain.calendarevent.vo.UpdateScheduleVo
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.tag.vo.TagVo
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.findByIdAndNotDeleted
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserStatus
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertNotNull

@CaramelDomainSpringBootTest
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

    @AfterEach
    fun tearDown() {
        tagContentMappingRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        scheduleEventRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    private fun setUpCouple(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id",
    ): Triple<User, User, Couple> {
        return createCouple(userRepository, coupleRepository, myPlatformId, partnerPlatformId)
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

    @DisplayName("일정 조회 시 일정 정보와 본문, 연관 태그까지 정상 조회된다.")
    @Test
    fun getSchedule() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        val (schedule, tags) = createScheduleWithTags(myUser, 10)

        // when
        val result = scheduleEventService.getSchedule(
            scheduleId = schedule.id,
            ownerCoupleId = couple.id,
        )

        // then
        assertThat(result.scheduleDetail.scheduleId).isEqualTo(schedule.id)
        assertThat(result.tags).containsExactlyInAnyOrderElementsOf(tags.map { TagVo.from(it) })
    }

    @DisplayName("일정 조회 시 파트너의 일정도 일정 정보와 본문, 연관 태그까지 정상 조회된다.")
    @Test
    fun getSchedule_WithPartnersSchedule() {
        // given
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        val (schedule, tags) = createScheduleWithTags(partnerUser, 10)

        // when
        val result = scheduleEventService.getSchedule(
            scheduleId = schedule.id,
            ownerCoupleId = couple.id,
        )

        // then
        assertThat(result.scheduleDetail.scheduleId).isEqualTo(schedule.id)
        assertThat(result.tags).containsExactlyInAnyOrderElementsOf(tags.map { TagVo.from(it) })
    }

    @DisplayName("일정 조회 시 다른 커플의 일정이라면 예외가 발생한다.")
    @Test
    fun getSchedule_WithOtherCouplesMemo() {
        // given
        val (otherUser1, _, _) = createCouple(userRepository, coupleRepository, "other1", "other2")
        val (_, _, couple) = createCouple(userRepository, coupleRepository)
        val (schedule, tags) = createScheduleWithTags(otherUser1, 0)

        // when
        val result = assertThrows<ScheduleAccessDeniedException> {
            scheduleEventService.getSchedule(
                scheduleId = schedule.id,
                ownerCoupleId = couple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ScheduleExceptionCode.COUPLE_NOT_MATCHED)
    }

    @DisplayName("일정 조회 시 존재하지 않는 일정이라면 예외가 발생한다.")
    @Test
    fun getSchedule_WithIllegalScheduleId() {
        // given
        val (_, _, couple) = createCouple(userRepository, coupleRepository)
        val illegalScheduleId = 0L

        // when
        val result = assertThrows<ScheduleNotFoundException> {
            scheduleEventService.getSchedule(
                scheduleId = illegalScheduleId,
                ownerCoupleId = couple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ScheduleExceptionCode.SCHEDULE_NOT_FOUND)
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
        val scheduleVo = UpdateScheduleVo(
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
            scheduleVo = scheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdWithContent(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(scheduleVo.title)
            assertThat(content.contentDetail.description).isEqualTo(scheduleVo.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(scheduleVo.startTimeZone!!.toZoneId())
            assertThat(startDateTime).isEqualTo(scheduleVo.startDateTime!!.withoutNano)
            assertThat(endTimeZone).isEqualTo(scheduleVo.endTimeZone!!.toZoneId())
            assertThat(endDateTime).isEqualTo(scheduleVo.endDateTime!!.withoutNano)
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
        val scheduleVo = UpdateScheduleVo(
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
            scheduleVo = scheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdWithContent(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(content.contentDetail.title).isEqualTo(scheduleVo.title)
            assertThat(content.contentDetail.description).isEqualTo(scheduleVo.description)
            assertThat(content.contentDetail.isCompleted).isTrue()
            assertThat(startTimeZone).isEqualTo(scheduleVo.startTimeZone!!.toZoneId())
            assertThat(startDateTime).isEqualTo(scheduleVo.startDateTime!!.withoutNano)
            assertThat(endTimeZone).isEqualTo(scheduleVo.endTimeZone!!.toZoneId())
            assertThat(endDateTime).isEqualTo(scheduleVo.endDateTime!!.withoutNano)
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
        val scheduleVo = UpdateScheduleVo(
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
            scheduleVo = scheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

        // then
        val updatedScheduleEvent = scheduleEventRepository.findByIdOrNull(oldSchedule.id)!!
        updatedScheduleEvent.run {
            assertThat(id).isEqualTo(oldSchedule.id)
            assertThat(endDateTime).isEqualTo(scheduleVo.startDateTime!!.endOfDay.withoutNano)
            assertThat(endTimeZone).isEqualTo(scheduleVo.startTimeZone!!.toZoneId())
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
        val scheduleVo = UpdateScheduleVo(
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
                scheduleVo = scheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
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
        val scheduleVo = UpdateScheduleVo(
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
                scheduleVo = scheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
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
        val scheduleVo = UpdateScheduleVo(
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
                scheduleVo = scheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
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
        val scheduleVo = UpdateScheduleVo(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW.plusDays(1),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleAccessDeniedException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                scheduleVo = scheduleVo,
                currentUserId = otherUser.id,
                currentUserCoupleId = otherUser.couple?.id ?: error("couple id가 없습니다"),
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

        val scheduleVo = UpdateScheduleVo(
            selectedDate = DateTimeUtil.localNow().toLocalDate(),
            title = "valid title",
            description = "valid description",
            isCompleted = false,
            startDateTime = NOW.plusDays(1),
            startTimeZone = DateTimeUtil.UTC_ZONE_ID.id,
        )

        // when, then
        val exception = assertThrows<ScheduleAccessDeniedException> {
            scheduleEventService.updateSchedule(
                scheduleId = oldSchedule.id,
                scheduleVo = scheduleVo,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
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

        val scheduleVo = UpdateScheduleVo(
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
            scheduleVo = scheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

        // then
        val updatedTagMappings = tagContentMappingRepository.findAllWithTagByContentId(oldContent.id)
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
        val scheduleVo = UpdateScheduleVo(
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
            scheduleVo = scheduleVo,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

        // then
        val deletedSchedule = scheduleEventRepository.findByIdAndNotDeleted(oldSchedule.id)
        assertThat(deletedSchedule).isNull()
        val content = contentRepository.findByIdAndNotDeleted(oldContent.id)
        assertNotNull(content)
        assertThat(content.type).isEqualTo(ContentType.MEMO)
        assertThat(content.contentDetail.title).isEqualTo(scheduleVo.title)
        assertThat(content.contentDetail.description).isEqualTo(scheduleVo.description)
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
        scheduleEventService.deleteSchedule(
            scheduleId = schedule.id,
            currentUserId = myUser.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        )

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
    fun getSchedules() {
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
        ).forEach {
            scheduleEventService.deleteSchedule(
                scheduleId = it.id,
                currentUserId = myUser.id,
                currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
            )
        }

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

        // when
        val scheduleDetailVoList = scheduleEventService.getSchedules(
            startDate = startDate,
            endDate = endDate,
            userTimeZone = userTimeZone.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        ).scheduleDetailVoList

        // then
        assertThat(scheduleDetailVoList).hasSize(numberOfEvents)

        val resultScheduleIds = scheduleDetailVoList.map { it.scheduleId }
        val savedScheduleIds = scheduleEvents.map { it.id }
        assertThat(resultScheduleIds).containsExactlyInAnyOrderElementsOf(savedScheduleIds)
    }

    @DisplayName("스케줄 조회 시 날짜 범위에 맞는 일정들이 조회된다.")
    @Test
    fun getSchedules_WithDailyAndWeeklyRequest() {
        // given
        val startDate = LocalDate.of(2025, 4, 1)
        val userTimeZone = ZoneId.of("Asia/Seoul")

        // 조회 대상 커플 데이터 생성
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val numberOfEvents = 30
        val scheduleEvents = mutableListOf<ScheduleEvent>()
        repeat(numberOfEvents) { idx ->
            val content =
                contentRepository.save(createContent(if (idx % 2 == 0) myUser else partnerUser, ContentType.SCHEDULE))
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

        // when
        val resultDaily = scheduleEventService.getSchedules(
            startDate = startDate,
            endDate = startDate,
            userTimeZone = userTimeZone.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        ).scheduleDetailVoList
        val resultWeekly = scheduleEventService.getSchedules(
            startDate = startDate,
            endDate = startDate.plusDays(6),
            userTimeZone = userTimeZone.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        ).scheduleDetailVoList

        // then
        assertThat(resultDaily).hasSize(1)
        assertThat(resultWeekly).hasSize(7)
    }

    @DisplayName("스케줄 조회 시 조회 범위 밖의 스케줄은 조회되지 않아야 한다.")
    @Test
    fun getSchedules_DateRangeWithoutEvents() {
        // given
        val startDate = LocalDate.of(2025, 6, 2)
        val userTimeZone = ZoneId.of("Asia/Seoul")

        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val content = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val event = scheduleEventRepository.save(
            ScheduleEvent(
                uid = "test-uid",
                startDateTime = startDate.toDateTime(),
                startTimeZone = userTimeZone,
                endDateTime = startDate.toDateTime().endOfDay,
                endTimeZone = userTimeZone,
                content = content
            )
        )

        // when
        val result = scheduleEventService.getSchedules(
            startDate = startDate.minusDays(1),
            endDate = startDate.minusDays(1),
            userTimeZone = userTimeZone.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        ).scheduleDetailVoList

        // then
        assertThat(result).hasSize(0)
    }

    @DisplayName("스케줄 조회 시 시작일, 종료일이 같다면 id 오름차순으로 정렬되어 반환된다.")
    @Test
    fun getSchedules_WithSameDateTimeSchedules() {
        // given
        val startDate = LocalDate.of(2025, 4, 1)
        val userTimeZone = ZoneId.of("Asia/Seoul")

        // 조회 대상 커플 데이터 생성
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val numberOfEvents = 10
        val scheduleEvents = mutableListOf<ScheduleEvent>()
        repeat(numberOfEvents) { idx ->
            val content =
                contentRepository.save(createContent(if (idx % 2 == 0) myUser else partnerUser, ContentType.SCHEDULE))
            val eventStartDate = startDate.plusDays(idx.toLong())
            val event = scheduleEventRepository.save(
                ScheduleEvent(
                    uid = "test-uid-${idx}",
                    startDateTime = startDate.toDateTime(),
                    startTimeZone = userTimeZone,
                    endDateTime = startDate.toDateTime().endOfDay,
                    endTimeZone = userTimeZone,
                    content = content
                )
            )
            scheduleEvents.add(event)
        }

        // when
        val result = scheduleEventService.getSchedules(
            startDate = startDate,
            endDate = startDate.plusDays(1),
            userTimeZone = userTimeZone.id,
            currentUserCoupleId = myUser.couple?.id ?: error("couple id가 없습니다"),
        ).scheduleDetailVoList

        // then
        assertThat(result).hasSize(10)
        assertThat(result.map { it.scheduleId }).isSortedAccordingTo(Comparator.naturalOrder())
    }

    private fun createScheduleWithTags(
        ownerUser: User,
        tagCount: Int = 10,
    ): Pair<ScheduleEvent, Set<Tag>> {
        val content = contentRepository.save(createContent(ownerUser, ContentType.SCHEDULE))
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
        return Pair(schedule, tags)
    }
}

internal fun createTags(
    tagNames: Set<String>,
    tagRepository: TagRepository,
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
    userStatus: UserStatus = UserStatus.SINGLE,
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
