package com.whatever.caramel.domain.notification.service.event.handler

import com.whatever.caramel.domain.couple.model.CoupleAnniversaryType
import com.whatever.caramel.domain.couple.service.CoupleAnniversaryService
import com.whatever.caramel.domain.couple.service.CoupleService
import com.whatever.caramel.domain.couple.service.event.dto.CoupleStartDateUpdateEvent
import com.whatever.caramel.domain.couple.vo.CoupleAnniversaryItem
import com.whatever.caramel.domain.couple.vo.CoupleDetailVo
import com.whatever.caramel.domain.couple.vo.MemberAnniversaryItem
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.AnniversaryNotificationScheduler
import com.whatever.caramel.domain.notification.service.event.handler.scheduler.AnniversaryNotificationSchedulerProvider
import com.whatever.caramel.domain.user.service.event.dto.UserBirthDateUpdateEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate

class AnniversaryUpdatedEventHandlerTest {

    private lateinit var mockCoupleAnniversaryService: CoupleAnniversaryService
    private lateinit var mockScheduledNotificationService: ScheduledNotificationService
    private lateinit var mockCoupleService: CoupleService
    private lateinit var mockSchedulerNth: AnniversaryNotificationScheduler
    private lateinit var mockSchedulerYearly: AnniversaryNotificationScheduler
    private lateinit var mockSchedulerBirthday: AnniversaryNotificationScheduler
    private lateinit var mockSchedulerProvider: AnniversaryNotificationSchedulerProvider

    private lateinit var handler: AnniversaryUpdatedEventHandler

    private val today = LocalDate.of(2025, 8, 7)

    @BeforeEach
    fun setUp() {
        mockCoupleAnniversaryService = mockk()
        mockScheduledNotificationService = mockk(relaxUnitFun = true) // 반환값이 Unit인 함수는 자동 mock
        mockCoupleService = mockk()

        mockSchedulerNth = mockk(relaxUnitFun = true)
        every { mockSchedulerNth.supports() } returns CoupleAnniversaryType.N_TH_DAY
        mockSchedulerYearly = mockk(relaxUnitFun = true)
        every { mockSchedulerYearly.supports() } returns CoupleAnniversaryType.YEARLY
        mockSchedulerBirthday = mockk(relaxUnitFun = true)
        every { mockSchedulerBirthday.supports() } returns CoupleAnniversaryType.BIRTHDAY

        mockSchedulerProvider = AnniversaryNotificationSchedulerProvider(listOf(mockSchedulerNth, mockSchedulerYearly, mockSchedulerBirthday))

        handler = AnniversaryUpdatedEventHandler(
            scheduledNotificationService = mockScheduledNotificationService,
            coupleAnniversaryService = mockCoupleAnniversaryService,
            coupleService = mockCoupleService,
            schedulerProvider = mockSchedulerProvider,
        )
    }

    @DisplayName("커플 시작일 변경 이벤트 처리")
    @ParameterizedTest
    @CsvSource("true", "false")
    fun handle_WhenCoupleStartDateUpdated(hasOldDate: Boolean) {
        // given
        val oldDate = if (hasOldDate) today.minusYears(2) else null
        val newDate = today.minusDays(100)
        val memberIds = setOf(1L, 2L)
        val event = CoupleStartDateUpdateEvent(oldDate = oldDate, newDate = newDate, memberIds = memberIds)

        val oldAnniversaries = listOf(CoupleAnniversaryItem(type = CoupleAnniversaryType.N_TH_DAY, date = oldDate ?: today, label = "old"))
        val newAnniversaries = listOf(CoupleAnniversaryItem(type = CoupleAnniversaryType.N_TH_DAY, date = newDate, label = "new"))
        if (oldDate != null) {
            stubFindAnniversaries(oldDate, oldAnniversaries)
            every { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) } returns 2
        }
        stubFindAnniversaries(newDate, newAnniversaries)

        // when
        handler.handle(event, today)

        // then
        if (hasOldDate) {
            verify(exactly = 1) {
                mockScheduledNotificationService.deleteScheduledNotifications(
                    targetUserIds = memberIds,
                    notificationTypes = setOf(NotificationType.ANNIVERSARY_HUNDRED)
                )
            }
        } else {
            verify(exactly = 0) { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) }
        }
        verify(exactly = 1) { mockSchedulerNth.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerBirthday.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerYearly.schedule(any(), any()) }
    }

    @DisplayName("사용자 생일이 변경되었을 때, 기존 알림을 삭제하고 새 알림을 등록한다.")
    @ParameterizedTest
    @CsvSource("true", "false")
    fun handle_WhenUserBirthDateUpdated(hasOldDate: Boolean) {
        // given
        val oldDate = if (hasOldDate) today.minusYears(2) else null
        val newDate = today.minusYears(1)
        val ownerId = 1L
        val partnerId = 2L
        val event = UserBirthDateUpdateEvent(oldDate = oldDate, newDate = newDate, userId = ownerId, userNickname = "test", coupleId = 1L)

        val mockCoupleDetailVo = mockk<CoupleDetailVo> {
            every { myInfo.id } returns event.userId
            every { partnerInfo.id } returns partnerId
        }
        every { mockCoupleService.getCoupleAndMemberInfo(any(), any()) } returns mockCoupleDetailVo

        if (oldDate != null) {
            val oldBirthDateItem = MemberAnniversaryItem(type = CoupleAnniversaryType.BIRTHDAY, label = "test label", ownerId = event.userId, ownerNickname = event.userNickname, date = oldDate)
            stubFindBirthDate(oldDate, listOf(oldBirthDateItem))
            every { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) } returns 2
        }
        val newBirthDateItem = MemberAnniversaryItem(type = CoupleAnniversaryType.BIRTHDAY, label = "test label", ownerId = event.userId, ownerNickname = event.userNickname, date = newDate)
        stubFindBirthDate(newDate, listOf(newBirthDateItem))

        // when
        handler.handle(event, today)

        // then
        if (hasOldDate) {
            verify(exactly = 1) {
                mockScheduledNotificationService.deleteScheduledNotifications(setOf(ownerId), setOf(NotificationType.MY_BIRTHDAY))
            }
            verify(exactly = 1) {
                mockScheduledNotificationService.deleteScheduledNotifications(setOf(partnerId), setOf(NotificationType.PARTNER_BIRTHDAY))
            }
        } else {
            verify(exactly = 0) { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) }
        }
        verify(exactly = 1) { mockSchedulerBirthday.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerYearly.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerNth.schedule(any(), any()) }
    }

    @DisplayName("생일 알림 삭제 시, 존재하는 커플 멤버만 필터링하여 삭제한다.")
    @Test
    fun handle_WhenUserBirthDateUpdatedAndInvalidCoupleMemberId() {
        // given
        val oldDate = today.minusYears(2)
        val ownerId = 1L
        val partnerId = 2L
        val event = UserBirthDateUpdateEvent(
            oldDate = oldDate,
            newDate = today.minusYears(1),
            userId = ownerId,
            coupleId = 1L,
            userNickname = "owner"
        )

        val coupleDetailWithNoPartner = mockk<CoupleDetailVo> {
            every { myInfo.id } returns 2L  // Illegal owner id
            every { partnerInfo.id } returns partnerId
        }
        every { mockCoupleService.getCoupleAndMemberInfo(any(), any()) } returns coupleDetailWithNoPartner

        val oldBirthDateItem = MemberAnniversaryItem(type = CoupleAnniversaryType.BIRTHDAY, label = "test label", ownerId = event.userId, ownerNickname = event.userNickname, date = oldDate)
        stubFindBirthDate(oldDate, listOf(oldBirthDateItem))
        val newBirthDateItem = MemberAnniversaryItem(type = CoupleAnniversaryType.BIRTHDAY, label = "test label", ownerId = event.userId, ownerNickname = event.userNickname, date = event.newDate)
        stubFindBirthDate(event.newDate, listOf(newBirthDateItem))

        every { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) } returns 2

        // when
        handler.handle(event, today)

        // then
        verify(exactly = 0) {
            mockScheduledNotificationService.deleteScheduledNotifications(targetUserIds = setOf(ownerId), notificationTypes = any())
        }
        verify(exactly = 1) {
            mockScheduledNotificationService.deleteScheduledNotifications(targetUserIds = setOf(partnerId), notificationTypes = setOf(NotificationType.PARTNER_BIRTHDAY))
        }
        verify(exactly = 1) { mockSchedulerBirthday.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerYearly.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerNth.schedule(any(), any()) }
    }

    @DisplayName("매년 반복되는 기념일이 변경되었을 때, 기존 알림을 삭제하고 새 알림을 등록한다.")
    @Test
    fun handle_WhenYearlyAnniversaryUpdated() {
        // given
        val oldDate = today.minusYears(2)
        val newDate = today.minusYears(1)
        val event = CoupleStartDateUpdateEvent(oldDate = oldDate, newDate = newDate, memberIds = setOf(1L, 2L))

        val oldAnniversaries = listOf(CoupleAnniversaryItem(type = CoupleAnniversaryType.YEARLY, date = oldDate, label = "2주년"))
        stubFindAnniversaries(oldDate, oldAnniversaries)
        every {
            mockScheduledNotificationService.deleteScheduledNotifications(
                targetUserIds = event.memberIds,
                notificationTypes = setOf(NotificationType.ANNIVERSARY_YEARLY),
            )
        } returns 2

        val newAnniversaries = listOf(CoupleAnniversaryItem(type = CoupleAnniversaryType.YEARLY, date = newDate, label = "3주년"))
        stubFindAnniversaries(newDate, newAnniversaries)

        // when
        handler.handle(event, today)

        // then
        verify(exactly = 1) {
            mockScheduledNotificationService.deleteScheduledNotifications(
                targetUserIds = event.memberIds,
                notificationTypes = setOf(NotificationType.ANNIVERSARY_YEARLY),
            )
        }
        verify(exactly = 1) { mockSchedulerYearly.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerNth.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerBirthday.schedule(any(), any()) }
    }

    @DisplayName("기념일이 없는 날짜의 이벤트를 처리할 때, 아무 알림도 스케줄링하지 않는다")
    @Test
    fun handle_WhenNoAnniversaryOnTargetDate() {
        // given
        val newDate = today.minusDays(99)
        val event = CoupleStartDateUpdateEvent(oldDate = null, newDate = newDate, memberIds = setOf(1L, 2L))

        stubFindAnniversaries(newDate, emptyList())

        // when
        handler.handle(event, today)

        // then
        verify(exactly = 0) { mockScheduledNotificationService.deleteScheduledNotifications(any(), any()) }

        verify(exactly = 0) { mockSchedulerNth.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerYearly.schedule(any(), any()) }
        verify(exactly = 0) { mockSchedulerBirthday.schedule(any(), any()) }
    }

    private fun stubFindAnniversaries(date: LocalDate, anniversaries: List<CoupleAnniversaryItem>) {
        every { mockCoupleAnniversaryService.getYearly(date, any(), any()) } returns anniversaries.filter { it.type == CoupleAnniversaryType.YEARLY }
        every { mockCoupleAnniversaryService.get100ThDay(date, any(), any()) } returns anniversaries.filter { it.type == CoupleAnniversaryType.N_TH_DAY }
    }

    private fun stubFindBirthDate(date: LocalDate, birthDates: List<MemberAnniversaryItem>) {
        every { mockCoupleAnniversaryService.getBirthDay(any(), any(), date, any(), any()) } returns birthDates
    }
}
