package com.whatever.caramel.domain.balancegame.service.event

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.couple.service.event.ExcludeAsyncConfigBean
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.firebase.service.event.dto.BalanceGameOptionChosenEvent
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test

@CaramelDomainSpringBootTest
class BalanceGameNotificationEventPublishTest @Autowired constructor(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ExcludeAsyncConfigBean() {

    @MockitoBean
    private lateinit var firebaseService: FirebaseService

    @DisplayName("BalanceGameOptionChosenEvent가 발행되고 commit이 완료되면 파트너에게만 알림이 전송된다.")
    @Test
    @Transactional
    fun publishEvent_WhenCommit_SendsNotificationToPartner() {
        // given
        val userId = 1L
        val partnerId = 2L
        val event = BalanceGameOptionChosenEvent(
            userId = userId,
            memberIds = setOf(userId, partnerId),
        )

        // when
        applicationEventPublisher.publishEvent(event)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        // then
        verify(firebaseService, times(1))
            .sendNotification(eq(setOf(partnerId)), any())
    }

    @DisplayName("BalanceGameOptionChosenEvent가 발행되어도 rollback되면 알림이 전송되지 않는다.")
    @Test
    @Transactional
    fun publishEvent_WhenRollback_DoesNotSendNotification() {
        // given
        val event = BalanceGameOptionChosenEvent(
            userId = 1L,
            memberIds = setOf(1L, 2L),
        )

        // when
        applicationEventPublisher.publishEvent(event)
        TestTransaction.flagForRollback()
        TestTransaction.end()

        // then
        verify(firebaseService, never())
            .sendNotification(any(), any())
    }
}
