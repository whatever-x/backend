package com.whatever.caramel.domain.content.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.content.exception.ContentExceptionCode.UPDATE_CONFLICT
import com.whatever.caramel.domain.content.exception.ContentIllegalStateException
import com.whatever.caramel.domain.content.model.Content
import com.whatever.caramel.domain.content.model.ContentDetail
import com.whatever.caramel.domain.content.repository.ContentRepository
import com.whatever.caramel.domain.content.tag.model.Tag
import com.whatever.caramel.domain.content.tag.model.TagContentMapping
import com.whatever.caramel.domain.content.vo.ContentType
import com.whatever.caramel.domain.content.vo.UpdateContentRequestVo
import com.whatever.caramel.domain.couple.model.Couple
import com.whatever.caramel.domain.couple.repository.CoupleRepository
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserGender
import com.whatever.caramel.domain.user.model.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@CaramelDomainSpringBootTest
class ContentServiceRetryableTest {
    @MockitoBean
    private lateinit var contentRepository: ContentRepository

    @MockitoBean
    private lateinit var coupleRepository: CoupleRepository

    @Autowired
    private lateinit var contentService: ContentService

    @DisplayName("updateContent 에서 OptimisticLockingException이 발생하면, Recover 에서 ContentIllegalStateException 를 던진다")
    @Test
    fun updateContent_whenOptimisticLockingException_thenContentIllegalStateException() {
        val memoId = 1L
        val coupleId = 1L
        val userId = 1L
        val contentId = 1L

        val contentRequestVo = UpdateContentRequestVo(
            title = "a",
            description = "b",
            isCompleted = false,
            tagList = listOf(),
            dateTimeInfo = null,
        )
        val user = createTestUser(id = userId)
        val memo = createTestContent(
            id = memoId,
            user = user,
            title = "완료된 메모",
            description = "완료된 메모 내용",
            isCompleted = true
        )
        val expectedException = OptimisticLockingFailureException("error")
        whenever(contentRepository.findContentByIdAndType(id = contentId, type = ContentType.MEMO)).thenReturn(memo)
        whenever(coupleRepository.findByIdWithMembers(any())).thenThrow(expectedException)

        val exception = assertThrows<ContentIllegalStateException> {
            contentService.updateContent(
                contentId = contentId,
                requestVo = contentRequestVo,
                userCoupleId = coupleId,
                userId = userId,
            )
        }

        assertThat(exception.errorCode).isEqualTo(UPDATE_CONFLICT)
    }

    @DisplayName("deleteContent 에서 OptimisticLockingException이 발생하면, Recover 에서 ContentIllegalStateException 를 던진다")
    @Test
    fun deleteContent_whenOptimisticLockingException_thenContentIllegalStateException() {
        val contentId = 1L

        val expectedException = OptimisticLockingFailureException("error")
        whenever(contentRepository.findContentByIdAndType(any(), any())).thenThrow(expectedException)

        val exception = assertThrows<ContentIllegalStateException> {
            contentService.deleteContent(contentId = contentId)
        }

        assertThat(exception.errorCode).isEqualTo(UPDATE_CONFLICT)
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
