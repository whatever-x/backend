package com.whatever.domain.content.service

import com.whatever.domain.calendarevent.scheduleevent.repository.ScheduleEventRepository
import com.whatever.domain.content.controller.dto.request.CreateContentRequest
import com.whatever.domain.content.controller.dto.request.DateTimeInfoDto
import com.whatever.domain.content.controller.dto.request.TagIdDto
import com.whatever.domain.content.controller.dto.request.UpdateContentRequest
import com.whatever.domain.content.controller.dto.response.TagDto
import com.whatever.domain.content.exception.ContentAccessDeniedException
import com.whatever.domain.content.exception.ContentExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.domain.content.exception.ContentExceptionCode.MEMO_NOT_FOUND
import com.whatever.domain.content.exception.ContentNotFoundException
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
import com.whatever.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.domain.firebase.service.FirebaseService
import com.whatever.domain.user.model.User
import com.whatever.domain.user.repository.UserRepository
import com.whatever.global.security.util.SecurityUtil
import com.whatever.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class ContentServiceTest @Autowired constructor(
    private val contentService: ContentService,
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val tagRepository: TagRepository,
    private val tagContentMappingRepository: TagContentMappingRepository,
    private val scheduleEventRepository: ScheduleEventRepository,
    private val coupleRepository: CoupleRepository,
) : ExcludeAsyncConfigBean() {

    private lateinit var securityUtilMock: AutoCloseable
    private lateinit var testUser: User
    private lateinit var testPartnerUser: User
    private lateinit var testCouple: Couple

    @MockitoBean//(reset = MockReset.AFTER)
    private lateinit var firebaseService: FirebaseService

    @BeforeEach
    fun setUp() {
        val (myUser, partnerUser, couple) = createCouple(userRepository, coupleRepository)
        testUser = myUser
        testPartnerUser = partnerUser
        testCouple = couple
        securityUtilMock = mockStatic(SecurityUtil::class.java)
        whenever(SecurityUtil.getCurrentUserId()).thenReturn(testUser.id)
        whenever(SecurityUtil.getCurrentUserCoupleId()).thenReturn(couple.id)
    }

    @AfterEach
    fun tearDown() {
        securityUtilMock.close()

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
        tags: List<Tag> = emptyList()
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

    @DisplayName("메모 생성 시 fcm 전송 메서드가 실행된다.")
    @Test
    fun createContent() {
        // given
        val request = CreateContentRequest(
            title = "test-title",
            description = "test-desc",
            isCompleted = false,
        )

        // when
        val result = contentService.createContent(request)

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
            ownerCoupleId = testCouple.id
        )

        // then
        assertThat(result.id).isEqualTo(testMemo.id)
        assertThat(result.title).isEqualTo(testMemo.contentDetail.title)
        assertThat(result.description).isEqualTo(testMemo.contentDetail.description)
        assertThat(result.tagList).containsExactlyInAnyOrderElementsOf(tags.map { TagDto.from(it) })
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
            ownerCoupleId = testCouple.id
        )

        // then
        assertThat(result.id).isEqualTo(testMemo.id)
        assertThat(result.title).isEqualTo(testMemo.contentDetail.title)
        assertThat(result.description).isEqualTo(testMemo.contentDetail.description)
        assertThat(result.tagList).containsExactlyInAnyOrderElementsOf(tags.map { TagDto.from(it) })
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
                ownerCoupleId = testCouple.id
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
                memoId = 0L ,  // illegal memo id
                ownerCoupleId = testCouple.id
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
        val request = UpdateContentRequest(
            title = newTitle,
            description = newDesc,
            isCompleted = newCompleted,
            tagList = emptyList(),
            dateTimeInfo = null // Keep as MEMO
        )

        // when
        val response = contentService.updateContent(memo.id, request)

        // then
        val updatedContent = contentRepository.findByIdOrNull(memo.id)!!
        assertThat(response.contentId).isEqualTo(memo.id)
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

        val request = UpdateContentRequest(
            title = memo.contentDetail.title ?: "",
            description = memo.contentDetail.description ?: "",
            isCompleted = memo.contentDetail.isCompleted,
            tagList = listOf(TagIdDto(tag2.id), TagIdDto(tag3.id)),
            dateTimeInfo = null
        )

        // when
        contentService.updateContent(memo.id, request)

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
        val request = UpdateContentRequest(title = "Any", description = "Any", isCompleted = false)

        // when & then
        assertThrows<ContentNotFoundException> {
            contentService.updateContent(nonExistentId, request)
        }
    }

    @DisplayName("콘텐츠 수정: 메모에 DateTimeInfo 정보를 추가하여 일정을 생성하고, partnerUser에게 fcm 알림을 전송한다.")
    @Test
    fun updateContent_WithDateTimeInfo() {
        // given
        val memo = createTestMemo(title = "Original Title", description = "Original Desc", isCompleted = false)
        val newTitle = "Updated Title"
        val newDesc = "Updated Desc"
        val request = UpdateContentRequest(
            title = newTitle,
            description = newDesc,
            isCompleted = memo.contentDetail.isCompleted,
            dateTimeInfo = DateTimeInfoDto(
                startDateTime = DateTimeUtil.localNow(),
                startTimezone = DateTimeUtil.KST_ZONE_ID.toString(),
            )
        )

        // when
        val response = contentService.updateContent(memo.id, request)

        // then
        val scheduleEvent = scheduleEventRepository.findAll().single()
        assertThat(response.contentId).isEqualTo(scheduleEvent.id)
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
}

fun TagContentMappingRepository.findAllByContentIdIncludingDeleted(contentId: Long): List<TagContentMapping> {
    return this.findAll().filter { it.content.id == contentId }
}
