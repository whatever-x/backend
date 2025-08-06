package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.UNSUPPORTED_NOTIFICATION_TYPE
import com.whatever.caramel.domain.notification.exception.UnsupportedNotificationTypeException
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class NotificationMessageProviderTest {

    private lateinit var mockMyBirthdayMessageGenerator: NotificationMessageGenerator
    private lateinit var mockPartnerBirthdayGenerator: NotificationMessageGenerator
    private lateinit var provider: NotificationMessageProvider

    @BeforeEach
    fun setUp() {
        mockMyBirthdayMessageGenerator = mockk< MyBirthdayMessageGenerator>().also {
            every { it.supports() } returns NotificationType.MY_BIRTHDAY
        }
        mockPartnerBirthdayGenerator = mockk<PartnerBirthdayMessageGenerator>().also {
            every { it.supports() } returns NotificationType.PARTNER_BIRTHDAY
        }

        provider = NotificationMessageProvider(
            generators = listOf(mockMyBirthdayMessageGenerator, mockPartnerBirthdayGenerator)
        )
    }

    @DisplayName("주어진 타입에 맞는 Generator를 찾아 메시지를 반환한다.")
    @Test
    fun provide() {
        // given
        val type = NotificationType.MY_BIRTHDAY
        val parameter = BirthDayParameter(
            label = "test label",
            birthdayMemberNickname = "test nick",
            isMyBirthday = true,
        )
        val expectedMessage = NotificationMessageVo(
            type = type,
            title = "title for test",
            body = "body for test",
        )
        every { mockMyBirthdayMessageGenerator.generate(any()) } returns expectedMessage

        // when
        val resultMessage = provider.provide(type, parameter)

        // then
        assertThat(resultMessage).isEqualTo(resultMessage)

        verify(exactly = 1) { mockMyBirthdayMessageGenerator.generate(parameter) }
        verify(exactly = 0) { mockPartnerBirthdayGenerator.generate(any()) }
    }

    @DisplayName("지원하지 않은 타입을 받을경우 예외를 반환한다.")
    @Test
    fun provide_WithUnsupportedType_ThenThrowsException() {
        // given
        val unsupportedType = NotificationType.ANNIVERSARY_YEARLY
        val parameter = HundredAnniversaryParameter(
            label = "test label",
        )

        // when
        val result = assertThrows<UnsupportedNotificationTypeException> {
            provider.provide(unsupportedType, parameter)
        }

        // then
        assertThat(result.errorCode).isEqualTo(UNSUPPORTED_NOTIFICATION_TYPE)
    }
}
