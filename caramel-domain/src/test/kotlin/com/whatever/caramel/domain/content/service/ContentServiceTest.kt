package com.whatever.caramel.domain.content.service

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.calendarevent.vo.DateTimeInfoVo
import com.whatever.caramel.domain.content.exception.ContentAccessDeniedException
import com.whatever.caramel.domain.content.exception.ContentExceptionCode
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.MEMO_NOT_FOUND
import com.whatever.caramel.domain.content.exception.ContentIllegalArgumentException
import com.whatever.caramel.domain.content.exception.ContentNotFoundException
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.tag.vo.TagVo
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.ContentAssignee
import com.whatever.caramel.domain.content.vo.CreateContentRequestVo
import com.whatever.caramel.domain.content.vo.UpdateContentRequestVo
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.Test

@CaramelDomainSpringBootTest
class ContentServiceTest @Autowired constructor(
    private val contentService: ContentService,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val coupleRepository: CoupleRepository,
) : ExcludeAsyncConfigBean() {

    private lateinit var testUser: User
    private lateinit var testPartnerUser: User
    private lateinit var testCouple: Couple

    @MockitoBean
    private lateinit var firebaseService: FirebaseService

    @BeforeEach
    fun setUp() {
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        testUser = myUser
        testPartnerUser = partnerUser
        testCouple = couple
    }

    @AfterEach
    fun tearDown() {
        scheduleEventRepository.deleteAllInBatch()
        tagContentMappingRepository.deleteAllInBatch()
        contentRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        userRepository.deleteAllInBatch()
        coupleRepository.deleteAllInBatch()
    }

    private fun createTestTag(label: String): Tag {
        return tagRepository.save(Tag(label = label))
    }

    private fun createTestMemo(
        user: User = testUser,
        title: String = "Test Memo",
        description: String? = "Memo Description",
        isCompleted: Boolean = false,
        tags: List<Tag> = emptyList(),
    ): Content {
        val contentDetail = ContentDetail(title = title, description = description, isCompleted = isCompleted)
        val content = contentRepository.save(
            Content(user = user, contentDetail = contentDetail, type = ContentType.MEMO)
        )
        if (tags.isNotEmpty()) {
            val mappings = tags.map { TagContentMapping(tag = it, content = content) }
            tagContentMappingRepository.saveAll(mappings)
        }
        return content
    }

    @DisplayName("메모 생성 시 제목만 null이라면 저장에 성공한다.")
    @Test
    fun createContent_WithNullTitle() {
        // given
        val requestVo = CreateContentRequestVo(
            title = null,
            description = "test-desc",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val result = contentService.createContent(
            contentRequestVo = requestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // then
        val savedContent = contentRepository.findByIdOrNull(result.id)
        requireNotNull(savedContent)

        with(savedContent.contentDetail) {
            assertThat(title).isNull()
            assertThat(description).isEqualTo(requestVo.description)
        }
    }

    @DisplayName("메모 생성 시 본문만 null이라면 저장에 성공한다.")
    @Test
    fun createContent_WithNullDescription() {
        // given
        val requestVo = CreateContentRequestVo(
            title = "test-title",
            description = null,
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val result = contentService.createContent(
            contentRequestVo = requestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // then
        val savedContent = contentRepository.findByIdOrNull(result.id)
        requireNotNull(savedContent)

        with(savedContent.contentDetail) {
            assertThat(title).isEqualTo(requestVo.title)
            assertThat(description).isNull()
        }
    }

    @DisplayName("메모 생성 시 제목과 본문 하나라도 Blank라면 저장에 실패한다.")
    @ParameterizedTest
    @CsvSource(
        value = [
            "title, ' '",
            "'  ' , description",
            "'  ' , '  '",
        ]
    )
    fun createContent_WithNullTitleAndDescription(title: String, description: String) {
        // given
        val requestVo = CreateContentRequestVo(
            title = title,
            description = description,
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.PARTNER,
        )

        // when
        val result = assertThrows<ContentIllegalArgumentException> {
            contentService.createContent(
                contentRequestVo = requestVo,
                userId = testUser.id,
                coupleId = testCouple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ContentExceptionCode.ILLEGAL_CONTENT_DETAIL)
        assertThat(contentRepository.findAll()).isEmpty()
    }

    @DisplayName("메모 생성 시 제목과 본문 모두 null이라면 저장에 실패한다.")
    @Test
    fun createContent_WithNullTitleAndDescription() {
        // given
        val requestVo = CreateContentRequestVo(
            title = null,
            description = null,
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val result = assertThrows<ContentIllegalArgumentException> {
            contentService.createContent(
                contentRequestVo = requestVo,
                userId = testUser.id,
                coupleId = testCouple.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(ContentExceptionCode.ILLEGAL_CONTENT_DETAIL)
        assertThat(contentRepository.findAll()).isEmpty()
    }

    @DisplayName("메모 생성 시 fcm 전송 메서드가 실행된다.")
    @Test
    fun createContent_WithSendNotification() {
        // given
        val requestVo = CreateContentRequestVo(
            title = "test-title",
            description = "test-desc",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val result = contentService.createContent(
            contentRequestVo = requestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // then
        assertThat(result.contentType).isEqualTo(ContentType.MEMO)
        verify(firebaseService, times(1))
            .sendNotification(
                targetUserIds = eq(setOf(testPartnerUser.id)),
                fcmNotification = any(),
            )
    }

    @DisplayName("메모를 조회할 경우 본문과 태그까지 정상적으로 조회된다.")
    @Test
    fun getMemo() {
        // given
        val tags = mutableListOf<Tag>()
        repeat(10) { i ->
            tags.add(createTestTag("label${i}"))
        }
        val testMemo = createTestMemo(
            user = testUser,
            tags = tags,
        )

        // when
        val result = contentService.getMemo(
            memoId = testMemo.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testUser.id,
        )

        // then
        assertThat(result.id).isEqualTo(testMemo.id)
        assertThat(result.title).isEqualTo(testMemo.contentDetail.title)
        assertThat(result.description).isEqualTo(testMemo.contentDetail.description)
        assertThat(result.tagList).containsExactlyInAnyOrderElementsOf(tags.map { TagVo.from(it) })
    }

    @DisplayName("메모를 조회할 때 파트너의 메모도 본문과 태그까지 정상적으로 조회된다.")
    @Test
    fun getMemo_WithPartnersMemo() {
        // given
        val tags = mutableListOf<Tag>()
        repeat(10) { i ->
            tags.add(createTestTag("label${i}"))
        }
        val testMemo = createTestMemo(
            user = testPartnerUser,
            tags = tags,
        )

        // when
        val result = contentService.getMemo(
            memoId = testMemo.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testUser.id,
        )

        // then
        assertThat(result.id).isEqualTo(testMemo.id)
        assertThat(result.title).isEqualTo(testMemo.contentDetail.title)
        assertThat(result.description).isEqualTo(testMemo.contentDetail.description)
        assertThat(result.tagList).containsExactlyInAnyOrderElementsOf(tags.map { TagVo.from(it) })
    }

    @DisplayName("메모를 조회할 때 다른 커플의 메모일 경우 얘외가 발생한다.")
    @Test
    fun getMemo_WithOtherCouplesMemo() {
        // given
        val (otherUser1, _, _) = createCouple(
            userRepository = userRepository,
            coupleRepository = coupleRepository,
            myPlatformUserId = "other-me",
            partnerPlatformUserId = "partner-me"
        )
        val testMemo = createTestMemo(user = otherUser1)

        // when
        val result = assertThrows<ContentAccessDeniedException> {
            contentService.getMemo(
                memoId = testMemo.id,
                ownerCoupleId = testCouple.id,
                requestUserId = testUser.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(COUPLE_NOT_MATCHED)
    }

    @DisplayName("메모를 조회할 때 존재하지 않는 메모라면 예외가 발생한다.")
    @Test
    fun getMemo_WithIllegalMemoId() {
        // given

        // when
        val result = assertThrows<ContentNotFoundException> {
            contentService.getMemo(
                memoId = 0L,  // illegal memo id
                ownerCoupleId = testCouple.id,
                requestUserId = testUser.id,
            )
        }

        // then
        assertThat(result.errorCode).isEqualTo(MEMO_NOT_FOUND)
    }

    @DisplayName("콘텐츠 수정: 메모의 제목, 설명, 완료 상태를 성공적으로 업데이트한다")
    @Test
    fun updateContent_MemoDetails() {
        // given
        val memo = createTestMemo(title = "Original Title", description = "Original Desc", isCompleted = false)
        val newTitle = "Updated Title"
        val newDesc = "Updated Desc"
        val newCompleted = true
        val requestVo = UpdateContentRequestVo(
            title = newTitle,
            description = newDesc,
            isCompleted = newCompleted,
            tagList = emptyList(),
            dateTimeInfo = null, // Keep as MEMO
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val response = contentService.updateContent(
            contentId = memo.id,
            requestVo = requestVo,
            userCoupleId = testCouple.id,
            userId = testUser.id,
        )

        // then
        val updatedContent = contentRepository.findByIdOrNull(memo.id)!!
        assertThat(response.id).isEqualTo(memo.id)
        assertThat(response.contentType).isEqualTo(ContentType.MEMO)
        assertThat(updatedContent.contentDetail.title).isEqualTo(newTitle)
        assertThat(updatedContent.contentDetail.description).isEqualTo(newDesc)
        assertThat(updatedContent.contentDetail.isCompleted).isEqualTo(newCompleted)
        assertThat(updatedContent.type).isEqualTo(ContentType.MEMO)
    }

    @DisplayName("콘텐츠 수정: 태그를 추가하고 제거한다")
    @Test
    fun updateContent_UpdateTags() {
        // given
        val tag1 = createTestTag("Tag1")
        val tag2 = createTestTag("Tag2")
        val tag3 = createTestTag("Tag3")
        val memo = createTestMemo(tags = listOf(tag1, tag2))

        val requestVo = UpdateContentRequestVo(
            title = memo.contentDetail.title ?: "",
            description = memo.contentDetail.description ?: "",
            isCompleted = memo.contentDetail.isCompleted,
            tagList = listOf(tag2.id, tag3.id),
            dateTimeInfo = null,
            contentAssignee = ContentAssignee.ME,
        )

        // when
        contentService.updateContent(
            contentId = memo.id,
            requestVo = requestVo,
            userCoupleId = testCouple.id,
            userId = testUser.id,
        )

        // then
        val updatedMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(memo.id)
        val updatedTagIds = updatedMappings.map { it.tag.id }.toSet()
        assertThat(updatedTagIds).containsExactlyInAnyOrder(tag2.id, tag3.id)

        val allMappingsIncludingDeleted =
            tagContentMappingRepository.findAllByContentIdIncludingDeleted(memo.id)
        val tag1Mapping = allMappingsIncludingDeleted.find { it.tag.id == tag1.id }
        assertThat(tag1Mapping).isNotNull
        assertThat(tag1Mapping!!.isDeleted).isTrue()
    }

    @DisplayName("콘텐츠 수정: 존재하지 않는 콘텐츠 ID로 수정 시 NotFoundException 발생")
    @Test
    fun updateContent_NotFound() {
        // given
        val nonExistentId = 9999L
        val requestVo = UpdateContentRequestVo(
            title = "Any",
            description = "Any",
            isCompleted = false,
            tagList = emptyList(),
            dateTimeInfo = null,
            contentAssignee = ContentAssignee.ME,
        )

        // when & then
        assertThrows<ContentNotFoundException> {
            contentService.updateContent(
                contentId = nonExistentId,
                requestVo = requestVo,
                userCoupleId = testCouple.id,
                userId = testUser.id,
            )
        }
    }

    @DisplayName("콘텐츠 수정: 메모에 DateTimeInfo 정보를 추가하여 일정을 생성하고, partnerUser에게 fcm 알림을 전송한다.")
    @Test
    fun updateContent_WithDateTimeInfo() {
        // given
        val memo = createTestMemo(title = "Original Title", description = "Original Desc", isCompleted = false)
        val newTitle = "Updated Title"
        val newDesc = "Updated Desc"
        val requestVo = UpdateContentRequestVo(
            title = newTitle,
            description = newDesc,
            isCompleted = memo.contentDetail.isCompleted,
            dateTimeInfo = DateTimeInfoVo(
                startDateTime = DateTimeUtil.localNow(),
                startTimezone = DateTimeUtil.KST_ZONE_ID.toString(),
            ),
            tagList = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // when
        val response = contentService.updateContent(
            contentId = memo.id,
            requestVo = requestVo,
            userCoupleId = testCouple.id,
            userId = testUser.id,
        )

        // then
        val scheduleEvent = scheduleEventRepository.findAll().single()
        assertThat(response.id).isEqualTo(scheduleEvent.id)
        assertThat(response.contentType).isEqualTo(ContentType.SCHEDULE)

        val updatedContent = contentRepository.findByIdOrNull(memo.id)!!
        assertThat(updatedContent.contentDetail.title).isEqualTo(newTitle)
        assertThat(updatedContent.contentDetail.description).isEqualTo(newDesc)
        assertThat(updatedContent.type).isEqualTo(ContentType.SCHEDULE)

        verify(firebaseService, times(1)).sendNotification(
            targetUserIds = eq(setOf(testPartnerUser.id)),
            fcmNotification = any(),
        )
    }

    @DisplayName("콘텐츠 삭제: 메모를 성공적으로 삭제한다 (Soft Delete)")
    @Test
    fun deleteContent_Memo() {
        // given
        val tag = createTestTag("ToDelete")
        val memo = createTestMemo(tags = listOf(tag))
        val mapping = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(memo.id).first()

        // when
        contentService.deleteContent(memo.id)

        // then
        val deletedContent = contentRepository.findById(memo.id)
        val deletedMapping = tagContentMappingRepository.findById(mapping.id)

        assertThat(deletedContent).isPresent

        assertThat(deletedMapping).isPresent
        val activeContent = contentRepository.findByIdOrNull(memo.id)
        val activeMappings = tagContentMappingRepository.findAllByContent_IdAndIsDeleted(memo.id)
        assertThat(activeMappings).isEmpty()
    }

    @DisplayName("콘텐츠 삭제: 존재하지 않는 콘텐츠 ID로 삭제 시도 시 오류 없이 완료된다")
    @Test
    fun deleteContent_NotFound() {
        // given
        val nonExistentId = 9998L

        // when & then
        assertThrows<ContentNotFoundException> {
            contentService.deleteContent(nonExistentId)
        }
        val content = contentRepository.findByIdOrNull(nonExistentId)
        assertThat(content).isNull()
    }

    @DisplayName("A가 PARTNER로 컨텐츠를 생성하면 B가 조회할 때 ME로 조회된다")
    @Test
    fun getMemo_WhenCreatedAsPARTNER_ThenViewedAsME() {
        // given
        val requestVo = CreateContentRequestVo(
            title = "Partner Content",
            description = "This is for partner",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.PARTNER,
        )

        // A가 PARTNER로 컨텐츠 생성
        val createdContent = contentService.createContent(
            contentRequestVo = requestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // when - B가 조회
        val result = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testPartnerUser.id,
        )

        // then - B의 관점에서는 ME로 조회되어야 함
        assertThat(result.contentAssignee).isEqualTo(ContentAssignee.ME)
        assertThat(result.id).isEqualTo(createdContent.id)
        assertThat(result.title).isEqualTo(requestVo.title)
        assertThat(result.description).isEqualTo(requestVo.description)
    }

    @DisplayName("A가 ME로 컨텐츠를 생성하면 B가 조회할 때 PARTNER로 조회된다")
    @Test
    fun getMemo_WhenCreatedAsME_ThenViewedAsPARTNER() {
        // given
        val requestVo = CreateContentRequestVo(
            title = "My Content",
            description = "This is for me",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.ME,
        )

        // A가 ME로 컨텐츠 생성
        val createdContent = contentService.createContent(
            contentRequestVo = requestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // when - B가 조회
        val result = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testPartnerUser.id,
        )

        // then - B의 관점에서는 PARTNER로 조회되어야 함
        assertThat(result.contentAssignee).isEqualTo(ContentAssignee.PARTNER)
        assertThat(result.id).isEqualTo(createdContent.id)
        assertThat(result.title).isEqualTo(requestVo.title)
        assertThat(result.description).isEqualTo(requestVo.description)
    }

    @DisplayName("A가 PARTNER로 컨텐츠를 생성 후, B가 ME로 수정 요청해도 실제로는 변경되지 않는다")
    @Test
    fun updateContent_WhenCreatedAsPARTNER_AndUpdatedAsME_ThenAssigneeNotChanged() {
        // given - A가 PARTNER로 컨텐츠 생성
        val createRequestVo = CreateContentRequestVo(
            title = "Partner Content",
            description = "This is for partner",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.PARTNER,
        )

        val createdContent = contentService.createContent(
            contentRequestVo = createRequestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // B가 조회할 때는 ME로 보임
        val retrievedByPartner = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testPartnerUser.id,
        )
        assertThat(retrievedByPartner.contentAssignee).isEqualTo(ContentAssignee.ME)

        // when - B가 ME로 수정 요청 (실제로는 변경되지 않아야 함)
        val updateRequestVo = UpdateContentRequestVo(
            title = "Updated Title",
            description = "Updated Description",
            isCompleted = true,
            tagList = emptyList(),
            dateTimeInfo = null,
            contentAssignee = ContentAssignee.ME, // B 관점에서는 ME로 요청
        )

        contentService.updateContent(
            contentId = createdContent.id,
            requestVo = updateRequestVo,
            userCoupleId = testCouple.id,
            userId = testPartnerUser.id,
        )

        // then - 실제 저장된 값은 여전히 PARTNER여야 함
        val updatedContent = contentRepository.findByIdOrNull(createdContent.id)!!
        assertThat(updatedContent.contentAssignee).isEqualTo(ContentAssignee.PARTNER)

        // A가 조회할 때는 여전히 PARTNER로 보여야 함
        val retrievedByOwner = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testUser.id,
        )
        assertThat(retrievedByOwner.contentAssignee).isEqualTo(ContentAssignee.PARTNER)
    }

    @DisplayName("A가 PARTNER로 컨텐츠를 생성 후, B가 PARTNER로 수정 요청하면 실제로는 ME로 변경된다")
    @Test
    fun updateContent_WhenCreatedAsPARTNER_AndUpdatedAsPARTNER_ThenAssigneeChangedToME() {
        // given - A가 PARTNER로 컨텐츠 생성
        val createRequestVo = CreateContentRequestVo(
            title = "Partner Content",
            description = "This is for partner",
            isCompleted = false,
            tags = emptyList(),
            contentAssignee = ContentAssignee.PARTNER,
        )

        val createdContent = contentService.createContent(
            contentRequestVo = createRequestVo,
            userId = testUser.id,
            coupleId = testCouple.id,
        )

        // when - B가 PARTNER로 수정 요청 (실제로는 ME로 변경되어야 함)
        val updateRequestVo = UpdateContentRequestVo(
            title = "Updated Title",
            description = "Updated Description",
            isCompleted = true,
            tagList = emptyList(),
            dateTimeInfo = null,
            contentAssignee = ContentAssignee.PARTNER, // B 관점에서는 PARTNER로 요청
        )

        contentService.updateContent(
            contentId = createdContent.id,
            requestVo = updateRequestVo,
            userCoupleId = testCouple.id,
            userId = testPartnerUser.id,
        )

        // then - 실제 저장된 값은 ME로 변경되어야 함
        val updatedContent = contentRepository.findByIdOrNull(createdContent.id)!!
        assertThat(updatedContent.contentAssignee).isEqualTo(ContentAssignee.ME)

        // A가 조회할 때는 ME로 보여야 함
        val retrievedByOwner = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testUser.id,
        )
        assertThat(retrievedByOwner.contentAssignee).isEqualTo(ContentAssignee.ME)

        // B가 조회할 때는 PARTNER로 보여야 함
        val retrievedByPartner = contentService.getMemo(
            memoId = createdContent.id,
            ownerCoupleId = testCouple.id,
            requestUserId = testPartnerUser.id,
        )
        assertThat(retrievedByPartner.contentAssignee).isEqualTo(ContentAssignee.PARTNER)
    }
}

fun TagContentMappingRepository.findAllByContentIdIncludingDeleted(contentId: Long): List<TagContentMapping> {
    return this.findAll().filter { it.content.id == contentId }
}
