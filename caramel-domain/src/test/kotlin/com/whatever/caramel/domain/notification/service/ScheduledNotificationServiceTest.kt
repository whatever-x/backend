package com.whatever.caramel.domain.notification.service

import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

class ScheduledNotificationServiceTest {
    private lateinit var mockScheduledNotificationRepository: ScheduledNotificationRepository
    private lateinit var service: ScheduledNotificationService

    @BeforeEach
    fun setUp() {
        mockScheduledNotificationRepository = mockk(relaxed = true)
        service = ScheduledNotificationService(mockScheduledNotificationRepository)
    }

    @DisplayName("알림 메시지가 주어졌을 때, ScheduledNotification 객체로 변환하여 저장을 요청한다")
    @Test
    fun scheduleNotifications() {
        // given
        val myId = 1L
        val partnerId = 2L
        val message = NotificationMessageVo(NotificationType.ANNIVERSARY_YEARLY, "1주년", "축하해요!")
        val messagesByUserId = mapOf(myId to message, partnerId to message)
        val notifyAt = LocalDate.of(2025, 8, 8).toDateTime()

        // when
        service.scheduleNotifications(messagesByUserId, notifyAt)

        // then
        verify(exactly = 1) { mockScheduledNotificationRepository.saveAll(any<List<ScheduledNotification>>()) }
    }

    @DisplayName("알림 메시지가 비어있을 경우, 저장 로직을 호출하지 않는다")
    @Test
    fun scheduleNotifications_WhenMessagesEmpty() {
        // given
        val emptyMessages = emptyMap<Long, NotificationMessageVo>()
        val notifyAt = LocalDateTime.now()

        // when
        service.scheduleNotifications(emptyMessages, notifyAt)

        // then
        verify(exactly = 0) { mockScheduledNotificationRepository.saveAll(any<List<ScheduledNotification>>()) }
    }

    @DisplayName("알림 삭제 요청 시, repository에 요청을 위임하고 삭제된 개수를 반환한다")
    @Test
    fun deleteScheduledNotifications() {
        // given
        val targetUserIds = setOf(1L, 2L)
        val notificationTypes = setOf(NotificationType.MY_BIRTHDAY, NotificationType.PARTNER_BIRTHDAY)
        val expectedDeletedCount = 2

        every {
            mockScheduledNotificationRepository.deleteAllByNotificationTypeInAndTargetUserIdIn(notificationTypes, targetUserIds)
        } returns expectedDeletedCount

        // when
        val result = service.deleteScheduledNotifications(targetUserIds, notificationTypes)

        // then
        verify(exactly = 1) {
            mockScheduledNotificationRepository.deleteAllByNotificationTypeInAndTargetUserIdIn(notificationTypes, targetUserIds)
        }
        assertThat(result).isEqualTo(expectedDeletedCount)
    }

    @DisplayName("알림 삭제 요청 시, 대상 유저나 타입이 비어있다면 delete 쿼리를 실행하지 않는다.")
    @ParameterizedTest
    @MethodSource("provideEmptyArgsForDeleteScheduledNotifications")
    fun deleteScheduledNotifications(targetUserIds: Set<Long>, notificationTypes: Set<NotificationType>) {
        // given
        val expectedDeletedCount = 0

        // when
        val result = service.deleteScheduledNotifications(targetUserIds, notificationTypes)

        // then
        verify(exactly = 0) {
            mockScheduledNotificationRepository.deleteAllByNotificationTypeInAndTargetUserIdIn(any(), any())
        }
        assertThat(result).isEqualTo(expectedDeletedCount)
    }

    companion object {
        @JvmStatic
        fun provideEmptyArgsForDeleteScheduledNotifications(): List<Arguments> {
            return listOf(
                Arguments.of(emptySet<Long>(), setOf(NotificationType.ANNIVERSARY_YEARLY)),
                Arguments.of(setOf(1L, 2L), emptySet<NotificationType>()),
            )
        }
    }
}