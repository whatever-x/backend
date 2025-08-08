package com.whatever.caramel.domain.notification.service.event.handler.scheduler

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode
import com.whatever.caramel.domain.notification.exception.UnsupportedCoupleAnniversaryTypeException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class AnniversaryNotificationSchedulerProviderTest {

    private lateinit var mockSchedulerYearly: AnniversaryNotificationScheduler
    private lateinit var mockSchedulerNthDay: AnniversaryNotificationScheduler
    private lateinit var provider: AnniversaryNotificationSchedulerProvider

    @BeforeEach
    fun setUp() {
        mockSchedulerYearly = mockk()
        every { mockSchedulerYearly.supports() } returns CoupleAnniversaryType.YEARLY

        mockSchedulerNthDay = mockk()
        every { mockSchedulerNthDay.supports() } returns CoupleAnniversaryType.N_TH_DAY

        val schedulers = listOf(mockSchedulerYearly, mockSchedulerNthDay)
        provider = AnniversaryNotificationSchedulerProvider(schedulers)
    }

    @Test
    @DisplayName("지원하는 기념일 타입에 대해 올바른 스케줄러를 반환한다")
    fun provide() {
        // when
        val actualScheduler = provider.provide(CoupleAnniversaryType.YEARLY)

        // then
        assertEquals(mockSchedulerYearly, actualScheduler)
    }

    @Test
    @DisplayName("지원하지 않는 기념일 타입에 대해서는 예외를 발생시킨다")
    fun provide_WhenUnsupportedCoupleAnniversaryType() {
        // given
        val unsupportedType = CoupleAnniversaryType.BIRTHDAY

        // when
        val result = assertThrows<UnsupportedCoupleAnniversaryTypeException> {
            provider.provide(unsupportedType)
        }

        // then
        assertEquals(result.errorCode, NotificationExceptionCode.UNSUPPORTED_COUPLE_ANNIV_TYPE)
    }
}