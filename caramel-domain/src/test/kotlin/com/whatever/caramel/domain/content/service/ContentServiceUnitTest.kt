package com.whatever.caramel.domain.content.service

import com.whatever.caramel.domain.calendarevent.repository.ScheduleEventRepository
import com.whatever.caramel.domain.content.exception.ContentAccessDeniedException
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.COUPLE_NOT_MATCHED
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.MEMO_NOT_FOUND
import com.whatever.caramel.domain.content.exception.ContentNotFoundException
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.tag.repository.TagContentMappingRepository
import com.whatever.caramel.domain.content.tag.repository.TagRepository
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ActiveProfiles("test")
class ContentServiceUnitTest {

    private val memoCreator = mockk<MemoCreator>()
    private val contentRepository = mockk<ContentRepository>()
    private val tagRepository = mockk<TagRepository>()
    private val tagContentMappingRepository = mockk<TagContentMappingRepository>()
    private val coupleRepository = mockk<CoupleRepository>()
    private val scheduleEventRepository = mockk<ScheduleEventRepository>()
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>()

    private val contentService = ContentService(
        memoCreator = memoCreator,
        contentRepository = contentRepository,
        tagRepository = tagRepository,
        tagContentMappingRepository = tagContentMappingRepository,
        coupleRepository = coupleRepository,
        scheduleEventRepository = scheduleEventRepository,
        applicationEventPublisher = applicationEventPublisher
    )

    @Test
    @DisplayName("메모 ID로 메모를 정상적으로 조회할 수 있다")
    fun getMemo_success() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(id = memoId, user = user)
        val tag = createTestTag(id = 1L, label = "테스트태그")
        val tagContentMapping = createTestTagContentMapping(id = 1L, tag = tag, content = memo)

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns listOf(tagContentMapping)

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("테스트 메모")
        assertThat(result.description).isEqualTo("테스트 메모 내용")
        assertThat(result.isCompleted).isFalse()
        assertThat(result.tagList).hasSize(1)
        assertThat(result.tagList[0].id).isEqualTo(1L)
        assertThat(result.tagList[0].label).isEqualTo("테스트태그")
    }

    @Test
    @DisplayName("존재하지 않는 메모 ID로 조회하면 ContentNotFoundException이 발생한다")
    fun getMemo_memoNotFound() {
        // given
        val memoId = -1L
        val coupleId = 1L

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns null

        // when
        val exception = assertThrows<ContentNotFoundException> {
            contentService.getMemo(
                memoId = memoId,
                ownerCoupleId = coupleId,
            )
        }

        // then
        assertThat(exception.errorCode).isEqualTo(MEMO_NOT_FOUND)
    }

    @Test
    @DisplayName("커플이 존재하지 않으면 ContentAccessDeniedException이 발생한다")
    fun getMemo_coupleNotFound() {
        // given
        val memoId = 1L
        val coupleId = -1L
        val userId = 1L

        val user = createTestUser(id = userId)
        val memo = createTestContent(id = memoId, user = user)

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns null

        // when
        val exception = assertThrows<ContentAccessDeniedException> {
            contentService.getMemo(
                memoId = memoId,
                ownerCoupleId = coupleId,
            )
        }

        // then
        assertThat(exception.errorCode).isEqualTo(COUPLE_NOT_MATCHED)
    }

    @Test
    @DisplayName("메모 작성자가 커플 멤버가 아니면 ContentAccessDeniedException이 발생한다")
    fun getMemo_memoAuthorNotInCouple() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L
        val otherUserId = 3L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val otherUser =
            createTestUser(id = otherUserId, nickname = "다른유저", userStatus = UserStatus.SINGLE, birthYear = 1992)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(
            id = memoId,
            user = otherUser,
            title = "다른 사용자의 메모",
            description = "다른 사용자가 작성한 메모"
        )

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple

        // when
        val exception = assertThrows<ContentAccessDeniedException> {
            contentService.getMemo(
                memoId = memoId,
                ownerCoupleId = coupleId,
            )
        }

        // then
        assertThat(exception.errorCode).isEqualTo(COUPLE_NOT_MATCHED)
    }

    @Test
    @DisplayName("명시적으로 커플 ID를 전달하여 메모를 조회할 수 있다")
    fun getMemo_withExplicitCoupleId() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(id = memoId, user = user, isCompleted = true)

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns emptyList()

        // when
        val result = contentService.getMemo(memoId, coupleId)

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("테스트 메모")
        assertThat(result.description).isEqualTo("테스트 메모 내용")
        assertThat(result.isCompleted).isTrue()
        assertThat(result.tagList).isEmpty()
    }

    @Test
    @DisplayName("파트너가 작성한 메모를 조회할 수 있다")
    fun getMemo_partnerMemo() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(
            id = memoId,
            user = partner,
            title = "파트너의 메모",
            description = "파트너가 작성한 메모"
        )

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns emptyList()

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("파트너의 메모")
        assertThat(result.description).isEqualTo("파트너가 작성한 메모")
        assertThat(result.isCompleted).isFalse()
        assertThat(result.tagList).isEmpty()
    }

    @Test
    @DisplayName("여러 태그가 있는 메모를 조회할 수 있다")
    fun getMemo_withMultipleTags() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(id = memoId, user = user)

        val tag1 = createTestTag(id = 1L, label = "개인")
        val tag2 = createTestTag(id = 2L, label = "업무")
        val tag3 = createTestTag(id = 3L, label = "중요")

        val tagMapping1 = createTestTagContentMapping(id = 1L, tag = tag1, content = memo)
        val tagMapping2 = createTestTagContentMapping(id = 2L, tag = tag2, content = memo)
        val tagMapping3 = createTestTagContentMapping(id = 3L, tag = tag3, content = memo)

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns listOf(
            tagMapping1,
            tagMapping2,
            tagMapping3
        )

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.tagList).hasSize(3)
        assertThat(result.tagList.map { it.id }).containsExactly(1L, 2L, 3L)
        assertThat(result.tagList.map { it.label }).containsExactly("개인", "업무", "중요")
    }

    @Test
    @DisplayName("제목만 있는 메모를 조회할 수 있다")
    fun getMemo_titleOnly() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(
            id = memoId,
            user = user,
            title = "제목만 있는 메모",
            description = null
        )

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns emptyList()

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("제목만 있는 메모")
        assertThat(result.description).isEqualTo("")
        assertThat(result.isCompleted).isFalse()
        assertThat(result.tagList).isEmpty()
    }

    @Test
    @DisplayName("내용만 있는 메모를 조회할 수 있다")
    fun getMemo_descriptionOnly() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(
            id = memoId,
            user = user,
            title = null,
            description = "내용만 있는 메모"
        )

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns emptyList()

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("")
        assertThat(result.description).isEqualTo("내용만 있는 메모")
        assertThat(result.isCompleted).isFalse()
        assertThat(result.tagList).isEmpty()
    }

    @Test
    @DisplayName("완료된 메모를 조회할 수 있다")
    fun getMemo_completedMemo() {
        // given
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val partnerId = 2L

        val user = createTestUser(id = userId)
        val partner = createTestUser(id = partnerId, nickname = "파트너", gender = UserGender.FEMALE, birthYear = 1991)
        val couple = createTestCouple(id = coupleId, user1 = user, user2 = partner)
        val memo = createTestContent(
            id = memoId,
            user = user,
            title = "완료된 메모",
            description = "완료된 메모 내용",
            isCompleted = true
        )

        every { contentRepository.findContentByIdAndType(memoId, ContentType.MEMO) } returns memo
        every { coupleRepository.findByIdWithMembers(coupleId) } returns couple
        every { tagContentMappingRepository.findAllWithTagByContentId(memoId) } returns emptyList()

        // when
        val result = contentService.getMemo(
            memoId = memoId,
            ownerCoupleId = coupleId,
        )

        // then
        assertThat(result.id).isEqualTo(memoId)
        assertThat(result.title).isEqualTo("완료된 메모")
        assertThat(result.description).isEqualTo("완료된 메모 내용")
        assertThat(result.isCompleted).isTrue()
        assertThat(result.tagList).isEmpty()
    }

    private fun createTestUser(
        id: Long = 1L,
        nickname: String = "테스트유저",
        gender: UserGender = UserGender.MALE,
        userStatus: UserStatus = UserStatus.SINGLE,
        birthYear: Int = 1990,
    ): User {
        return User(
            id = id,
            platform = LoginPlatform.TEST,
            platformUserId = UUID.randomUUID().toString(),
            nickname = nickname,
            gender = gender,
            userStatus = userStatus,
            birthDate = LocalDate.of(birthYear, 1, 1)
        )
    }

    private fun createTestCouple(
        id: Long = 1L,
        user1: User,
        user2: User,
    ): Couple {
        return Couple(id = id).apply {
            addMembers(user1, user2)
        }
    }

    private fun createTestContent(
        id: Long = 1L,
        user: User,
        title: String? = "테스트 메모",
        description: String? = "테스트 메모 내용",
        isCompleted: Boolean = false,
        type: ContentType = ContentType.MEMO,
    ): Content {
        val contentDetail = ContentDetail(
            title = title,
            description = description,
            isCompleted = isCompleted
        )

        val content = Content(
            id = id,
            user = user,
            contentDetail = contentDetail,
            type = type
        )

        try {
            val baseTimeEntityClass = Class.forName("com.whatever.caramel.domain.base.BaseTimeEntity")
            val createdAtField = baseTimeEntityClass.getDeclaredField("createdAt")
            createdAtField.isAccessible = true
            createdAtField.set(content, LocalDateTime.now())
        } catch (e: Exception) {
            // Reflection 실패 시 무시 (테스트 환경에서만 사용)
            println("Reflection failed: ${e.message}")
        }

        return content
    }

    private fun createTestTag(
        id: Long = 1L,
        label: String = "테스트태그",
    ): Tag {
        return Tag(id = id, label = label)
    }

    private fun createTestTagContentMapping(
        id: Long = 1L,
        tag: Tag,
        content: Content,
    ): TagContentMapping {
        return TagContentMapping(
            id = id,
            tag = tag,
            content = content
        )
    }
}
