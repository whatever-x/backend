package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.common.util.toDateTime
import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.vo.CoupleAnniversaryItem
import com.whatever.caramel.domain.notification.exception.InvalidSchedulingParameterException
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.INVALID_SCHEDULING_PARAMETER
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.domain.notification.service.message.NotificationMessageProvider
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.Test

class HundredDayAnniversaryNotificationSchedulerTest {
    private lateinit var mockScheduledNotificationService: ScheduledNotificationService
    private lateinit var mockNotificationMessageProvider: NotificationMessageProvider

    private lateinit var scheduler: HundredDayAnniversaryNotificationScheduler

    @BeforeEach
    fun setUp() {
        mockScheduledNotificationService = mockk<ScheduledNotificationService>()
        mockNotificationMessageProvider = mockk<NotificationMessageProvider>()

        scheduler = HundredDayAnniversaryNotificationScheduler(
            scheduledNotificationService = mockScheduledNotificationService,
            notificationMessageProvider = mockNotificationMessageProvider,
        )
    }

    @DisplayName("N_TH_DAY 타입을 지원하는지 확인한다.")
    @Test
    fun supports() {
        // when
        val supportType = scheduler.supports()

        // then
        assertThat(supportType).isEqualTo(CoupleAnniversaryType.N_TH_DAY)
    }

    @DisplayName("올바른 파라미터로 호출 시, 알림 메시지를 생성하고 스케줄링한다")
    @Test
    fun schedule() {
        // given
        val today = LocalDate.of(2025, 8, 7)
        val memberIds = setOf(1L, 2L)
        val notifyAt = today.toDateTime()
        val scheduleParameter = CoupleNotificationSchedulingParameter(
            anniversaryItem = CoupleAnniversaryItem(
                type = CoupleAnniversaryType.N_TH_DAY,
                date = today,
                label = "100일",
            ),
            memberIds = memberIds
        )

        val providedMessage = NotificationMessageVo(type = NotificationType.ANNIVERSARY_HUNDRED, title = "test", body = "test")
        every {
            mockNotificationMessageProvider.provide(
                type = any(),
                notificationMessageParameter = any(),
            )
        } returns providedMessage

        every {
            mockScheduledNotificationService.scheduleNotifications(messagesByUserId = any(), notifyAt = any())
        } just runs

        // when
        scheduler.schedule(notifyAt = notifyAt, schedulingParameter = scheduleParameter)

        // then
        verify(exactly = 1) {
            mockScheduledNotificationService.scheduleNotifications(
                messagesByUserId = memberIds.associateWith { providedMessage },
                notifyAt = notifyAt
            )
        }
    }

    @DisplayName("잘못된 타입의 파라미터를 받을 경우 예외를 반환한다.")
    @Test
    fun schedule_WithInvalidParameterType_ThenThrowException() {
        // given
        val today = LocalDate.of(2025, 8, 7)
        val notifyAt = today.toDateTime()
        val invalidParameter = mockk<BirthDateNotificationSchedulingParameter>()

        // when
        val result = assertThrows<InvalidSchedulingParameterException> {
            scheduler.schedule(notifyAt, invalidParameter)
        }

        // then
        assertThat(result.errorCode).isEqualTo(INVALID_SCHEDULING_PARAMETER)
        verify(exactly = 0) { mockNotificationMessageProvider.provide(any(), any()) }
        verify(exactly = 0) { mockScheduledNotificationService.scheduleNotifications(any(), any()) }
    }
}
