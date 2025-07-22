package com.whatever.caramel.domain.content.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.calendarevent.exception.ScheduleExceptionCode
import com.whatever.caramel.domain.calendarevent.exception.ScheduleIllegalArgumentException
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.scheduleevent.service.createCouple
import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo
import com.whatever.caramel.domain.content.exception.ContentAccessDeniedException
import com.whatever.caramel.domain.content.exception.ContentExceptionCode
import com.whatever.caramel.domain.content.exception.ContentIllegalArgumentException
import com.whatever.caramel.domain.content.exception.ContentNotFoundException
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.UpdateContentRequestVo
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.findByIdAndNotDeleted
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired

@CaramelDomainSpringBootTest
class MemoToScheduleConvertTest @Autowired constructor(
    private val contentService: ContentService,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val coupleRepository: CoupleRepository,
) {

    private lateinit var testUser: com.whatever.caramel.domain.user.model.User

    @AfterEach
    fun tearDown() {
        scheduleEventRepository.deleteAllInBatch()
        tagContentMappingRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
    }

    private fun setUpCoupleAndSecurity(
        myPlatformId: String = "my-user-id",
        partnerPlatformId: String = "partner-user-id",
    ): Triple<com.whatever.caramel.domain.user.model.User, com.whatever.caramel.domain.user.model.User, Couple> {
        val (myUser, partnerUser, couple) = createCouple(
            userRepository,
            coupleRepository,
            myPlatformId,
            partnerPlatformId
        )
        return Triple(myUser, partnerUser, couple)
    }

    @DisplayName("메모 업데이트 시 기간 정보가 존재하면 MEMO 타입 컨텐츠에서 Schedule 생성에 성공한다.")
    @Test
    fun updateContent() {
        // given
        val (myUser, _, couple) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))

        val requestVo = UpdateContentRequestVo(
            title = "Schedule Title",
            description = "Description Content",
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
                endDateTime = NOW.plusDays(2),
                endTimezone = DateTimeUtil.UTC_ZONE_ID.id,
            ),
            tagList = emptyList(),
        )

        // when
        val result = contentService.updateContent(
            contentId = memo.id,
            requestVo = requestVo,
            userCoupleId = couple.id,
            userId = myUser.id,
        )

        // then
        val scheduleEvent = scheduleEventRepository.findByIdAndNotDeleted(result.id)
        val content = contentRepository.findByIdAndNotDeleted(memo.id)
        require(scheduleEvent != null)
        require(content != null)
        assertThat(scheduleEvent.content.id).isEqualTo(memo.id)
        assertThat(content.type).isEqualTo(ContentType.SCHEDULE)
        assertThat(content.contentDetail.title).isEqualTo(requestVo.title)
        assertThat(content.contentDetail.description).isEqualTo(requestVo.description)
    }

    @DisplayName("메모 업데이트 시 존재하지 않거나 삭제된 memeId가 입력되면 예외가 발생한다.")
    @Test
    fun updateContent_WithIllegalContentId() {
        // given
        val (myUser, partnerUser, couple) = setUpCoupleAndSecurity()
        val requestVo = UpdateContentRequestVo(
            title = "title",
            description = "desc",
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
            ),
            tagList = emptyList(),
        )
        val invalidMemoId = 0L // 존재하지 않는 ID

        // when, then
        val exception = assertThrows<ContentNotFoundException> {
            contentService.updateContent(
                contentId = invalidMemoId,
                requestVo = requestVo,
                userCoupleId = couple.id,
                userId = myUser.id,
            )
        }
        assertThat(exception).hasMessage(ContentExceptionCode.MEMO_NOT_FOUND.message)
    }

    @DisplayName("메모 업데이트 시 memoId에 해당하는 content가 MEMO 타입이 아니라면 예외가 발생한다.")
    @Test
    fun updateContent_WithNonMemoContent() {
        // given
        val (myUser, _, couple) = setUpCoupleAndSecurity()
        val nonMemo = contentRepository.save(createContent(myUser, ContentType.SCHEDULE))
        val requestVo = UpdateContentRequestVo(
            title = "title",
            description = "desc",
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
            ),
            tagList = emptyList(),
        )
        // when, then
        val exception = assertThrows<ContentNotFoundException> {
            contentService.updateContent(
                contentId = nonMemo.id,
                requestVo = requestVo,
                userCoupleId = couple.id,
                userId = myUser.id
            )
        }
        assertThat(exception).hasMessage(ContentExceptionCode.MEMO_NOT_FOUND.message)
    }

    @DisplayName("메모 업데이트 시 다른 다른 커플의 메모라면 예외가 발생한다.")
    @Test
    fun updateContent_WithOtherCoupleMemo() {
        // given
        val (ownerUser, _, _) = createCouple(userRepository, coupleRepository, "owner", "partner-owner")
        val memo = contentRepository.save(createContent(ownerUser, ContentType.MEMO))
        val (otherUser, _, otherCouple) = createCouple(userRepository, coupleRepository, "other", "other2")

        val requestVo = UpdateContentRequestVo(
            title = "title",
            description = "desc",
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
            ),
            tagList = emptyList(),
        )
        // when, then
        val exception = assertThrows<ContentAccessDeniedException> {
            contentService.updateContent(
                contentId = memo.id,
                requestVo = requestVo,
                userCoupleId = otherCouple.id,
                userId = otherUser.id,
            )
        }
        assertThat(exception).hasMessage(ContentExceptionCode.COUPLE_NOT_MATCHED.message)
    }

    @DisplayName("메모 업데이트 시 title과 description이 모두 null 또는 blank이면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource(
        "test title, '      '",
        "test title, ''",
        "'        ', test description",
        "''        , test description",
        "          ,                 ",
    )
    fun updateContent_WithBlankOrNullTitleDescription(title: String?, description: String?) {
        // given
        val (myUser, _, couple) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val requestVo = UpdateContentRequestVo(
            title = title,
            description = description,
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
            ),
            tagList = emptyList(),
        )

        // when, then
        val exception = assertThrows<ContentIllegalArgumentException> {
            contentService.updateContent(
                contentId = memo.id,
                requestVo = requestVo,
                userCoupleId = couple.id,
                userId = myUser.id,
            )
        }
        assertThat(exception).hasMessage(ContentExceptionCode.ILLEGAL_CONTENT_DETAIL.message)
    }

    @DisplayName("메모 업데이트 시 endDateTime이 startDateTime보다 이르면 예외가 발생한다.")
    @Test
    fun updateContent_WithInvalidDuration() {
        // given
        val (myUser, _, couple) = setUpCoupleAndSecurity()
        val memo = contentRepository.save(createContent(myUser, ContentType.MEMO))
        val requestVo = UpdateContentRequestVo(
            title = "title",
            description = "desc",
            isCompleted = false,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = NOW,
                startTimezone = DateTimeUtil.UTC_ZONE_ID.id,
                endDateTime = NOW.minusDays(1),
                endTimezone = DateTimeUtil.UTC_ZONE_ID.id
            ),
            tagList = emptyList(),
        )
        // when, then
        val exception = assertThrows<ScheduleIllegalArgumentException> {
            contentService.updateContent(
                contentId = memo.id,
                requestVo = requestVo,
                userCoupleId = couple.id,
                userId = myUser.id,
            )
        }
        assertThat(exception).hasMessage(ScheduleExceptionCode.ILLEGAL_DURATION.message)
    }

    companion object {
        val NOW = DateTimeUtil.localNow()
    }
}
